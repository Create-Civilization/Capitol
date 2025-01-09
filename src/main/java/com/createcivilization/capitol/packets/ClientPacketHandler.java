package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.event.ClientEvents;
import com.createcivilization.capitol.packets.toserver.syncing.C2SRequestSync;
import com.createcivilization.capitol.screen.TeamStatisticsScreen;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import net.minecraftforge.api.distmarker.*;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

	private static final Minecraft instance = Minecraft.getInstance();
	public static Map<UUID, String> playerMap = new HashMap<>();

	public static void handleSyncedPacket(Runnable toRun) {
		try {
			toRun.run();
		} catch (Exception e) {
			System.out.println("Exception encountered on " + toRun.getClass().getCanonicalName() + " packet handling, dumping data and requesting synchronization.");
			TeamUtils.loadedTeams.clear();
			PacketHandler.sendToServer(new C2SRequestSync());
		}
	}

	public static void addTeam(Team toAdd) {
		TeamUtils.loadedTeams.add(toAdd);
	}

	public static void removeTeam(String toRemoveId) {
		handleSyncedPacket(() ->
			TeamUtils.loadedTeams.removeIf(team -> Objects.equals(team.getTeamId(), toRemoveId))
		);
	}

	public static void addChunk(String claimingTeamId, ChunkPos chunkToAdd, ResourceLocation dimension) {
		TeamUtils.claimChunk(TeamUtils.getTeam(claimingTeamId).getOrThrow(), dimension, chunkToAdd);
	}

	public static void removeChunk(String claimingTeamId, ChunkPos chunkToAdd, ResourceLocation dimension) {
		handleSyncedPacket(() ->
			TeamUtils.unclaimChunk(TeamUtils.getTeam(claimingTeamId).getOrThrow(), dimension, chunkToAdd)
		);
	}

	public static void openTeamStatistics(String teamId) {
		handleSyncedPacket(() -> {
			// This SHOULD throw if team is not loaded due to the server already checking for team existance
			// If it throws, client is out of sync, thus needs to be synced
			Team team = TeamUtils.getTeam(teamId).getOrThrow();
			instance.setScreen(new TeamStatisticsScreen(team));
		});
	}

	public static void addPlayerInfo(String playerName, UUID playerUUID) {
		ClientEvents.playerMap.put(playerUUID, playerName);
	}
}