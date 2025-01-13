package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.screen.TeamStatisticsScreen;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

	public static void addTeam(Team toAdd) {
		TeamUtils.loadedTeams.add(toAdd);
	}

	public static void removeTeam(String toRemoveId) {
		TeamUtils.loadedTeams.removeIf(team -> team.getTeamId().equals(toRemoveId));
	}

	public static void addChunk(String claimingTeamId, ChunkPos chunkToAdd, ResourceLocation dimension) {
		TeamUtils.claimChunk(TeamUtils.getTeam(claimingTeamId).getOrThrow(), dimension, chunkToAdd);
	}

	public static void removeChunk(String claimingTeamId, ChunkPos chunkToAdd, ResourceLocation dimension) {
		TeamUtils.unclaimChunk(TeamUtils.getTeam(claimingTeamId).getOrThrow(), dimension, chunkToAdd);
	}

	public static void openTeamStatistics(String teamId) {
		// This SHOULD throw if team is not loaded due to the server already checking for team existance
		// If it throws, client is out of sync, thus needs to be synced
		Team team = TeamUtils.getTeam(teamId).getOrThrow();
		ClientConstants.INSTANCE.setScreen(new TeamStatisticsScreen(team));
	}

	public static void addPlayerInfo(String playerName, UUID playerUUID) {
		ClientConstants.playerMap.put(playerUUID, playerName);
	}

	public static void handlePacket(Runnable run, NetworkEvent.Context ctx) {
		ctx.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(
				Dist.CLIENT,
				() -> run
			)
		);
		ctx.setPacketHandled(true);
	}
}