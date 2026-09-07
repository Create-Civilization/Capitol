package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.ClaimedChunk;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamMember;
import com.createcivilization.capitol.common.data.War;
import com.createcivilization.capitol.common.events.WarEvent;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.common.networking.packets.S2CSyncWars;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;	/**
	 * Migrated from the old War System's ChunkDataImpl mixin: contested edge chunks
	 * of either side of a war build up takeover progress while enemy players are
	 * standing in them (shown with a boss bar), decay when nobody is around, and
	 * get unclaimed once the progress hits the cap. Like the original, this is
	 * mutual - both sides' chunks can be taken over.
	 * <p>
	 * Runs once per second (every 20 ticks) and scales the configured per-tick
	 * values to match, so the old config semantics are preserved.
	 */
@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class WarTakeoverEvents {

	// how often (in ticks) takeover progress is updated. config values are per-tick,
	// so the deltas get multiplied by this to keep the same effective rates.
	private static final int PROCESS_INTERVAL = 20;
	// how often (in ticks) the per-team chunk lists are refreshed from the db
	private static final int CHUNK_CACHE_REFRESH = 100;

	// takeover progress + boss bar per (dimension, packed chunk pos)
	private static final Map<String, Map<Long, Integer>> takeoverProgress = new HashMap<>();
	private static final Map<String, Map<Long, ServerBossEvent>> takeoverBars = new HashMap<>();
	// cached claimed chunks per team, so the per-second tick doesn't hammer the db
	private static final Map<UUID, List<ClaimedChunk>> warTeamChunks = new HashMap<>();

	private static int tickCounter = 0;

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			PacketDistributor.sendToPlayer(player, new S2CSyncWars(DatabaseManager.database.getAllWars()));
		}
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Pre event) {
		CapitolDatabase database = DatabaseManager.database;
		List<War> wars = database.getAllWars();
		if (wars.isEmpty()) return;

		tickCounter++;
		if (tickCounter % PROCESS_INTERVAL != 0) return;
		if (tickCounter % CHUNK_CACHE_REFRESH == 0) refreshChunkCache(wars);

		for (War war : wars) processWar(event.getServer(), war);
		pruneEmptyBars();
	}

	private static void processWar(MinecraftServer server, War war) {
		CapitolDatabase database = DatabaseManager.database;

		Team declaring = database.getTeam(war.declaringTeamId());
		Team receiving = database.getTeam(war.receivingTeamId());
		if (declaring == null || receiving == null) return;

		List<Team> attackers = database.getTeamAndAllies(declaring);
		List<Team> defenders = database.getTeamAndAllies(receiving);

		Set<UUID> declaringSideIds = teamMemberIds(attackers);
		Set<UUID> receivingSideIds = teamMemberIds(defenders);

		// both sides' chunks are contestable, like the original: whoever holds a
		// chunk can lose it if the other side outnumbers them on the border
		for (Team holder : attackers) {
			for (ClaimedChunk chunk : chunksOfTeam(holder)) {
				processChunk(server, war, chunk, holder, declaringSideIds, receivingSideIds, true);
			}
		}
		for (Team holder : defenders) {
			for (ClaimedChunk chunk : chunksOfTeam(holder)) {
				processChunk(server, war, chunk, holder, declaringSideIds, receivingSideIds, false);
			}
		}
	}

	private static void processChunk(MinecraftServer server, War war, ClaimedChunk chunk, Team holder,
									Set<UUID> declaringSideIds, Set<UUID> receivingSideIds, boolean holderIsDeclaring) {
		CapitolDatabase database = DatabaseManager.database;
		ServerLevel level = getLevel(server, chunk.dimension());
		if (level == null) return;

		ChunkPos pos = new ChunkPos(chunk.chunkX(), chunk.chunkZ());
		if (!level.isLoaded(pos.getWorldPosition())) return;

		// still owned by the holder (chunk might have been taken since the cache refresh)
		Team owner = database.getChunkOwner(pos, level);
		if (owner == null || !owner.getId().equals(holder.getId())) return;

		// only the edge of a team's territory can be contested
		if (!isEdgeChunk(database, holder, chunk, level)) return;

		// the chunk's side is whichever side of the war the holder is on;
		// everyone on the other side is an enemy here
		Set<UUID> localIds = holderIsDeclaring ? declaringSideIds : receivingSideIds;
		Set<UUID> enemyIds = holderIsDeclaring ? receivingSideIds : declaringSideIds;

		List<ServerPlayer> localsInChunk = level.getPlayers(
			p -> p.chunkPosition().equals(pos) && localIds.contains(p.getUUID()));
		List<ServerPlayer> enemiesInChunk = level.getPlayers(
			p -> p.chunkPosition().equals(pos) && enemyIds.contains(p.getUUID()));

		int balance = enemiesInChunk.size() - localsInChunk.size();
		boolean anyInChunk = !localsInChunk.isEmpty() || !enemiesInChunk.isEmpty();

		String dimension = chunk.dimension();
		long packed = pos.toLong();
		Map<Long, Integer> dimProgress = takeoverProgress.computeIfAbsent(dimension, k -> new HashMap<>());
		Map<Long, ServerBossEvent> dimBars = takeoverBars.computeIfAbsent(dimension, k -> new HashMap<>());
		ServerBossEvent bar = dimBars.get(packed);
		if (bar == null && anyInChunk) {
			bar = new ServerBossEvent(
				Component.literal(pos.x + " " + pos.z),
				BossEvent.BossBarColor.RED,
				BossEvent.BossBarOverlay.NOTCHED_10
			);
			dimBars.put(packed, bar);
		}

		int progress = dimProgress.getOrDefault(packed, 0);
		int maxTakeover = CapitolConfig.MAX_WAR_TAKEOVER_AMOUNT.get();

		if (!anyInChunk) {
			// nobody is contesting: decay
			if (progress > 0) {
				int decay = (int) (CapitolConfig.WAR_TAKEOVER_DECREMENT.get()
					* CapitolConfig.WAR_DECAY_MULTIPLIER.get() * PROCESS_INTERVAL);
				progress = Math.max(0, progress - decay);
				if (bar != null) bar.setColor(BossEvent.BossBarColor.BLUE);
			}
		} else if (balance == 0) {
			// contested
			if (bar != null) bar.setColor(BossEvent.BossBarColor.WHITE);
		} else if (balance < 0) {
			// defenders outnumber attackers
			int decrement = CapitolConfig.WAR_TAKEOVER_DECREMENT.get() * (-balance) * PROCESS_INTERVAL;
			progress = Math.max(0, progress - decrement);
			if (bar != null) bar.setColor(BossEvent.BossBarColor.BLUE);
		} else {
			// attackers outnumber defenders
			int increment = CapitolConfig.WAR_TAKEOVER_INCREMENT.get() * balance * PROCESS_INTERVAL;
			progress = Math.min(maxTakeover, progress + increment);
			if (bar != null) bar.setColor(BossEvent.BossBarColor.RED);
		}

		if (bar != null) {
			Set<ServerPlayer> inChunk = new HashSet<>(localsInChunk);
			inChunk.addAll(enemiesInChunk);
			for (ServerPlayer player : inChunk) bar.addPlayer(player);
			bar.getPlayers().removeIf(player -> !inChunk.contains(player));
		}

		if (progress >= maxTakeover) {
			// the enemy wins the chunk: unclaim it from the holder
			if (bar != null) bar.removeAllPlayers();
			dimBars.remove(packed);
			dimProgress.remove(packed);

			database.unclaimChunk(holder, pos, level);
			PacketDistributor.sendToPlayersTrackingChunk(level, pos, new S2CChunkRemove(packed));
			NeoForge.EVENT_BUS.post(new WarEvent.ChunkTakenOverEvent(pos, level.dimension().location(), holder));
		} else {
			dimProgress.put(packed, progress);
			if (bar != null) bar.setProgress((float) progress / maxTakeover);
		}
	}

	// removes bars that nobody is watching anymore and have no progress left
	private static void pruneEmptyBars() {
		takeoverBars.entrySet().removeIf(dimEntry -> {
			Map<Long, ServerBossEvent> dimBars = dimEntry.getValue();
			dimBars.entrySet().removeIf(entry -> {
				ServerBossEvent bar = entry.getValue();
				boolean empty = bar.getPlayers().isEmpty();
				if (empty) bar.removeAllPlayers();
				return empty;
			});
			return dimBars.isEmpty();
		});
	}

	// drops all takeover state for a war's chunks (called when a war ends)
	public static void clearWarState(War war) {
		CapitolDatabase database = DatabaseManager.database;

		// both sides' chunks can hold takeover state, so clear them all
		List<Team> sides = new ArrayList<>();
		Team declaring = database.getTeam(war.declaringTeamId());
		Team receiving = database.getTeam(war.receivingTeamId());
		if (declaring != null) sides.addAll(database.getTeamAndAllies(declaring));
		if (receiving != null) sides.addAll(database.getTeamAndAllies(receiving));

		for (Team side : sides) {
			for (ClaimedChunk chunk : database.getTeamChunks(side)) {
				long packed = ChunkPos.asLong(chunk.chunkX(), chunk.chunkZ());
				Map<Long, ServerBossEvent> dimBars = takeoverBars.get(chunk.dimension());
				if (dimBars != null) {
					ServerBossEvent bar = dimBars.remove(packed);
					if (bar != null) bar.removeAllPlayers();
					if (dimBars.isEmpty()) takeoverBars.remove(chunk.dimension());
				}
				Map<Long, Integer> dimProgress = takeoverProgress.get(chunk.dimension());
				if (dimProgress != null) {
					dimProgress.remove(packed);
					if (dimProgress.isEmpty()) takeoverProgress.remove(chunk.dimension());
				}
			}
		}
		warTeamChunks.clear();
	}

	private static Set<UUID> teamMemberIds(List<Team> teams) {
		CapitolDatabase database = DatabaseManager.database;
		Set<UUID> ids = new HashSet<>();
		for (Team team : teams) {
			for (TeamMember member : database.getTeamMembers(team)) ids.add(member.playerUUID());
		}
		return ids;
	}

	private static List<ClaimedChunk> chunksOfTeam(Team team) {
		CapitolDatabase database = DatabaseManager.database;
		return warTeamChunks.getOrDefault(team.getId(), database.getTeamChunks(team));
	}

	private static void refreshChunkCache(List<War> wars) {
		CapitolDatabase database = DatabaseManager.database;
		warTeamChunks.clear();
		for (War war : wars) {
			Team declaring = database.getTeam(war.declaringTeamId());
			Team receiving = database.getTeam(war.receivingTeamId());
			if (declaring != null) {
				for (Team team : database.getTeamAndAllies(declaring)) {
					warTeamChunks.put(team.getId(), database.getTeamChunks(team));
				}
			}
			if (receiving != null) {
				for (Team team : database.getTeamAndAllies(receiving)) {
					warTeamChunks.put(team.getId(), database.getTeamChunks(team));
				}
			}
		}
	}

	// a chunk is on the edge of a team's claims if any of its 8 neighbors is not owned by that team
	private static boolean isEdgeChunk(CapitolDatabase database, Team team, ClaimedChunk chunk, Level level) {
		ChunkPos pos = new ChunkPos(chunk.chunkX(), chunk.chunkZ());
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				ChunkPos neighbor = new ChunkPos(pos.x + dx, pos.z + dz);
				Team owner = database.getChunkOwner(neighbor, level);
				if (owner == null || !owner.getId().equals(team.getId())) return true;
			}
		}
		return false;
	}

	private static ServerLevel getLevel(MinecraftServer server, String dimension) {
		ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension));
		return server.getLevel(dimKey);
	}

	private WarTakeoverEvents() {
		throw new AssertionError();
	}
}