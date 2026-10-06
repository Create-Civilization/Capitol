package com.createcivilization.capitol.client.journeymap;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Team;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.level.ChunkPos;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientJMClaims {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final ClientJMClaims INSTANCE = new ClientJMClaims();

	private ClientJMClaims() {}

	// this points at something like:
	// run/journeymap/data/mp/<server>/claims/claims.json
	// or run/journeymap/data/sp/<world>/claims/claims.json
	private Path claimsFile;
	// key is chunkpos packed as long, value is the team owning it
	private final Map<Long, Team> claims = new ConcurrentHashMap<>();
	// key is chunkpos packed as long, value is the capitol block id it falls under
	private final Map<Long, Long> capitolBlockIds = new ConcurrentHashMap<>();
	// lazy load so we dont hit disk unless we actually need it
	private boolean loaded;
	// if dirty is true we write the json back out
	private boolean dirty;

	public static ClientJMClaims instance() {
		return INSTANCE;
	}

	public synchronized void setJourneyMapDataPath(File addonDataModPath) {
		if (addonDataModPath == null) return;
		Path path = addonDataModPath.toPath().normalize();
		Path addonDataDir = path.getParent();
		if (addonDataDir == null) return;
		Path worldDir = addonDataDir.getParent();
		if (worldDir == null) return;

		Path nextClaimsFile = worldDir.resolve("claims").resolve("claims.json");
		if (claimsFile != null && claimsFile.equals(nextClaimsFile)) return;

		boolean hadExistingFile = claimsFile != null;
		if (hadExistingFile) {
			flush();
		}

		claimsFile = nextClaimsFile;
		loaded = false;
		if (hadExistingFile) {
			claims.clear();
			capitolBlockIds.clear();
			dirty = false;
		} else if (dirty) {
			flush();
		}
	}

	public synchronized Map<ChunkPos, Team> snapshot() {
		ensureLoaded();
		Map<ChunkPos, Team> out = new HashMap<>(claims.size());
		for (Map.Entry<Long, Team> e : claims.entrySet()) {
			out.put(new ChunkPos(e.getKey()), e.getValue());
		}
		return out;
	}

	public synchronized Map<ChunkPos, Long> blockIdSnapshot() {
		ensureLoaded();
		Map<ChunkPos, Long> out = new HashMap<>(capitolBlockIds.size());
		for (Map.Entry<Long, Long> e : capitolBlockIds.entrySet()) {
			out.put(new ChunkPos(e.getKey()), e.getValue());
		}
		return out;
	}

	public synchronized int signature() {
		ensureLoaded();
		return claims.entrySet().hashCode() + capitolBlockIds.entrySet().hashCode();
	}

	public synchronized void upsert(ChunkPos pos, Team team) {
		upsert(pos, team, null);
	}

	public synchronized void upsert(ChunkPos pos, Team team, Long capitolBlockId) {
		Objects.requireNonNull(pos, "pos");
		Objects.requireNonNull(team, "team");
		ensureLoaded();
		claims.put(pos.toLong(), team);
		if (capitolBlockId == null) {
			capitolBlockIds.remove(pos.toLong());
		} else {
			capitolBlockIds.put(pos.toLong(), capitolBlockId);
		}
		dirty = true;
	}

	public synchronized void remove(ChunkPos pos) {
		if (pos == null) return;
		ensureLoaded();
		if (claims.remove(pos.toLong()) != null || capitolBlockIds.remove(pos.toLong()) != null) {
			dirty = true;
		}
	}

	public synchronized void flush() {
		if (!dirty) return;
		if (claimsFile == null) return;
		ensureLoaded();
		writeFile();
		dirty = false;
	}

	private void ensureLoaded() {
		if (claimsFile == null) return;
		if (loaded) return;
		readFile();
		loaded = true;
	}

	private void readFile() {
		if (claimsFile == null) return;
		if (!Files.exists(claimsFile)) {
			writeFile();
			return;
		}
		try {
			String json = Files.readString(claimsFile, StandardCharsets.UTF_8);
			JsonObject root = GSON.fromJson(json, JsonObject.class);
			if (root == null) return;

			JsonArray arr = root.getAsJsonArray("claims");
			if (arr == null) return;

			for (JsonElement el : arr) {
				if (!el.isJsonObject()) continue;
				JsonObject obj = el.getAsJsonObject();
				JsonElement chunkEl = obj.get("chunk");
				JsonObject teamObj = obj.getAsJsonObject("team");
				if (chunkEl == null || teamObj == null) continue;

				long chunk = chunkEl.getAsLong();
				Team team = teamFromJson(teamObj);
				if (team == null) continue;
				claims.put(chunk, team);
				// older claims files dont have this field, then we just have no divider
				JsonElement blockEl = obj.get("capitolBlockId");
				if (blockEl != null && !blockEl.isJsonNull()) {
					capitolBlockIds.put(chunk, blockEl.getAsLong());
				}
			}
		} catch (Exception e) {
			Capitol.LOGGER.error("Failed to read JourneyMap claims file {}", claimsFile, e);
		}
	}

	private void writeFile() {
		if (claimsFile == null) return;

		JsonObject root = new JsonObject();
		root.addProperty("version", 2);
		root.addProperty("updatedAt", Instant.now().toString());
		JsonArray arr = new JsonArray();
		for (Map.Entry<Long, Team> e : claims.entrySet()) {
			JsonObject obj = new JsonObject();
			obj.addProperty("chunk", e.getKey());
			obj.add("team", teamToJson(e.getValue()));
			Long blockId = capitolBlockIds.get(e.getKey());
			if (blockId != null) {
				obj.addProperty("capitolBlockId", blockId);
			}
			arr.add(obj);
		}
		root.add("claims", arr);

		try {
			Files.createDirectories(claimsFile.getParent());
			Files.writeString(claimsFile, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException e) {
			Capitol.LOGGER.error("Failed to write JourneyMap claims file {}", claimsFile, e);
		}
	}

	private static JsonObject teamToJson(Team team) {
		JsonObject obj = new JsonObject();
		obj.addProperty("id", team.getId().toString());
		obj.addProperty("name", team.getName());
		obj.addProperty("tag", team.getTag());
		obj.addProperty("color", team.getColor().getRGB());
		return obj;
	}

	private static Team teamFromJson(JsonObject obj) {
		try {
			UUID id = UUID.fromString(obj.get("id").getAsString());
			String name = obj.get("name").getAsString();
			String tag = obj.get("tag").getAsString();
			Color color = new Color(obj.get("color").getAsInt(), true);
			return Team.builder()
				.id(id)
				.name(name)
				.tag(tag)
				.color(color)
				.currentClaims(0)
				.maxClaims(0)
				.teamPermissions(0L)
				.description("")
				.createdAt(0L)
				.build();
		} catch (Exception e) {
			return null;
		}
	}
}