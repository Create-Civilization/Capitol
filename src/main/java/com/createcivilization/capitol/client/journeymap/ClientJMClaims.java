package com.createcivilization.capitol.client.journeymap;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Team;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.fml.loading.FMLPaths;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public final class ClientJMClaims {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final ClientJMClaims INSTANCE = new ClientJMClaims();

	// this points at something like:
	// run/journeymap/data/mp/<server>/claims/claims.json
	// or run/journeymap/data/sp/<world>/claims/claims.json
	private Path claimsFile;
	// key is chunkpos packed as long, value is the team owning it
	private final Map<Long, Team> claims = new HashMap<>();
	// lazy load so we dont hit disk unless we actually need it
	private boolean loaded;
	// if dirty is true we write the json back out
	private boolean dirty;

	public static ClientJMClaims instance() {
		return INSTANCE;
	}

	public synchronized Map<ChunkPos, Team> snapshot() {
		ensureLoaded();
		Map<ChunkPos, Team> out = new HashMap<>(claims.size());
		for (Map.Entry<Long, Team> e : claims.entrySet()) {
			out.put(new ChunkPos(e.getKey()), e.getValue());
		}
		return out;
	}

	public synchronized void upsert(ChunkPos pos, Team team) {
		Objects.requireNonNull(pos, "pos");
		Objects.requireNonNull(team, "team");
		ensureLoaded();
		claims.put(pos.toLong(), team);
		dirty = true;
	}

	public synchronized void remove(ChunkPos pos) {
		if (pos == null) return;
		ensureLoaded();
		if (claims.remove(pos.toLong()) != null) {
			dirty = true;
		}
	}

	public synchronized void flushIfDirty() {
		if (!dirty) return;
		ensureLoaded();
		writeFile();
		dirty = false;
	}

	private void ensureLoaded() {
		Path resolved = resolveClaimsFile();
		if (resolved == null) return;
		if (claimsFile == null || !claimsFile.equals(resolved)) {
			claimsFile = resolved;
			loaded = false;
			claims.clear();
			dirty = false;
		}
		if (loaded) return;
		readFile();
		loaded = true;
	}

	private Path resolveClaimsFile() {
		Path dataRoot = FMLPaths.GAMEDIR.get().resolve("journeymap").resolve("data");
		boolean singleplayer = Minecraft.getInstance().hasSingleplayerServer();
		Path modeRoot = dataRoot.resolve(singleplayer ? "sp" : "mp");
		if (!Files.isDirectory(modeRoot)) return null;

		Path worldDir = newestDirectory(modeRoot);
		if (worldDir == null) return null;

		Path claimsDir = worldDir.resolve("claims");
		try {
			Files.createDirectories(claimsDir);
		} catch (IOException e) {
			Capitol.LOGGER.error("Failed to create JourneyMap claims directory {}", claimsDir, e);
			return null;
		}
		return claimsDir.resolve("claims.json");
	}

	private static Path newestDirectory(Path root) {
		try (Stream<Path> stream = Files.list(root)) {
			return stream
				.filter(Files::isDirectory)
				.max(Comparator.comparingLong(ClientJMClaims::lastModifiedSafe))
				.orElse(null);
		} catch (IOException e) {
			return null;
		}
	}

	private static long lastModifiedSafe(Path path) {
		try {
			return Files.getLastModifiedTime(path).toMillis();
		} catch (IOException e) {
			return 0L;
		}
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
			}
		} catch (Exception e) {
			Capitol.LOGGER.error("Failed to read JourneyMap claims file {}", claimsFile, e);
		}
	}

	private void writeFile() {
		if (claimsFile == null) return;

		JsonObject root = new JsonObject();
		root.addProperty("version", 1);
		root.addProperty("updatedAt", Instant.now().toString());
		JsonArray arr = new JsonArray();
		for (Map.Entry<Long, Team> e : claims.entrySet()) {
			JsonObject obj = new JsonObject();
			obj.addProperty("chunk", e.getKey());
			obj.add("team", teamToJson(e.getValue()));
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

