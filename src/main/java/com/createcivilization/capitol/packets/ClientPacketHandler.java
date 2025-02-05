package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.screen.TeamStatisticsScreen;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

	public static void addTeam(Team toAdd) {
		TeamUtils.loadedTeams.add(toAdd);
		ClientConstants.chunksDirty = true;
	}

	public static void removeTeam(String toRemoveId) {
		TeamUtils.loadedTeams.removeIf(team -> team.getTeamId().equals(toRemoveId));
		ClientConstants.chunksDirty = true;
	}

	public static void addChunk(String claimingTeamId, ChunkPos chunkToAdd, ResourceLocation dimension) {
		TeamUtils.claimChunk(TeamUtils.getTeam(claimingTeamId).getOrThrow(), dimension, chunkToAdd);
		ClientConstants.chunksDirty = true;
	}

	public static void removeChunks(String teamId, ChunkPos pos, ResourceLocation dimension) {
		TeamUtils.unclaimChunk(TeamUtils.getTeam(teamId).getOrThrow(), dimension, pos); // DO NOT SWITCH THIS METHOD OUT. It is handled already.
		ClientConstants.toResetChunksTeamIds.add(teamId);
		ClientConstants.chunksDirty = true;
	}

	public static void removeChunks(String teamId, List<ChunkPos> pos, ResourceLocation dimension) {
		TeamUtils.unclaimChunks(TeamUtils.getTeam(teamId).getOrThrow(), dimension, pos); // DO NOT SWITCH THIS METHOD OUT. It is handled already.
		ClientConstants.toResetChunksTeamIds.add(teamId);
		ClientConstants.chunksDirty = true;
	}

	public static void openTeamStatistics(String teamId) {
		// This SHOULD throw if team is not loaded due to the server already checking for team existence
		// If it throws, client is out of sync, thus needs to be synced
		Team team = TeamUtils.getTeam(teamId).getOrThrow();
		ClientConstants.INSTANCE.setScreen(new TeamStatisticsScreen(team));
	}

	public static void handlePacket(Runnable run, NetworkEvent.Context ctx) {
		ctx.enqueueWork(
			() -> DistHelper.runWhenOnClient(
				() -> run
			)
		);
		ctx.setPacketHandled(true);
	}

	public static void removeCapitol(Team.CapitolData capitolData, ResourceLocation dimension, String teamId) {
		Team team = TeamUtils.getTeam(teamId).getOrThrow();
		System.out.println(team.getDimensionalData(dimension).getCapitolDataList().size());
		System.out.println(team.getDimensionalData(dimension).getCapitolDataList().contains(capitolData));
		team.getDimensionalData(dimension).removeCapitolData(capitolData);
		System.out.println(team.getDimensionalData(dimension).getCapitolDataList().size());
		ClientConstants.toResetChunksTeamIds.add(teamId);
		ClientConstants.chunksDirty = true;
	}
}