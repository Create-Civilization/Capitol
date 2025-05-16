package com.createcivilization.capitol.packets.toclient;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.gui.screen.BookMenu;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import net.neoforged.api.distmarker.*;

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

	public static void removeChunks(ResourceLocation dimension, List<ChunkPos> pos) {
		Team team = TeamUtils.getTeam(pos.getFirst(), dimension).getOrThrow();
		TeamUtils.unclaimChunks(team, dimension, pos); // DO NOT SWITCH THIS METHOD OUT. It is handled already.
		ClientConstants.toResetChunksTeamIds.add(team.getTeamId());
		ClientConstants.chunksDirty = true;
	}

	public static void openTeamStatistics(String teamId) {
		// This SHOULD throw if team is not loaded due to the server already checking for team existence
		// If it throws, client is out of sync, thus needs to be synced
		Team team = TeamUtils.getTeam(teamId).getOrThrow();
		ClientConstants.INSTANCE.setScreen(new BookMenu(3,0));
	}

	public static void handlePacket(Runnable run, Object ctx) {
//		ctx.enqueueWork(
//			() -> DistHelper.runWhenOnClient(
//				() -> run
//			)
//		);
//		ctx.setPacketHandled(true);
	}

	public static void removeCapitol(Team.CapitolData capitolData, ResourceLocation dimension, String teamId) {
		Team team = TeamUtils.getTeam(teamId).getOrThrow();
		team.getDimensionalData(dimension).removeCapitolData(capitolData);
		ClientConstants.toResetChunksTeamIds.add(teamId);
		ClientConstants.chunksDirty = true;
	}
}