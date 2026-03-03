package com.createcivilization.capitol.old.old.payloads.toclient;

import com.createcivilization.capitol.old.old.constants.ClientConstants;
import com.createcivilization.capitol.old.old.gui.screen.BookMenu;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.team.War;

import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import net.neoforged.api.distmarker.*;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {

	public static void addTeam(OldTeam toAdd) {
		DataManager.TeamData.LOADED_OLD_TEAMS.add(toAdd);
		ClientConstants.chunksDirty = true;
	}

	public static void removeTeam(String toRemoveId) {
		DataManager.TeamData.LOADED_OLD_TEAMS.removeIf(team -> team.getTeamId().equals(toRemoveId));
		ClientConstants.chunksDirty = true;
	}

	public static void addChunk(String claimingTeamId, ChunkPos chunkToAdd, ResourceLocation dimension) {
		TeamUtils.claimChunk(TeamUtils.getTeam(claimingTeamId).getOrThrow(), dimension, chunkToAdd);
		ClientConstants.chunksDirty = true;
	}

	public static void removeChunks(ResourceLocation dimension, List<ChunkPos> pos) {
		OldTeam oldTeam = TeamUtils.getTeam(pos.getFirst(), dimension).getOrThrow();
		TeamUtils.unclaimChunks(oldTeam, dimension, pos); // DO NOT SWITCH THIS METHOD OUT. It is handled already.
		ClientConstants.toResetChunksTeamIds.add(oldTeam.getTeamId());
		ClientConstants.chunksDirty = true;
	}

	public static void openTeamStatistics(String teamId) {
		// This SHOULD throw if receivingOldTeam is not loaded due to the server already checking for receivingOldTeam existence
		// If it throws, client is out of sync, thus needs to be synced
		OldTeam oldTeam = TeamUtils.getTeam(teamId).getOrThrow();
		ClientConstants.INSTANCE.setScreen(new BookMenu(3,0));
	}

	public static void removeCapitol(OldTeam.CapitolData capitolData, ResourceLocation dimension, String teamId) {
		OldTeam oldTeam = TeamUtils.getTeam(teamId).getOrThrow();
		oldTeam.getDimensionalData(dimension).removeCapitolData(capitolData);
		ClientConstants.toResetChunksTeamIds.add(teamId);
		ClientConstants.chunksDirty = true;
	}

	public static void addWar(OldTeam declaringOldTeam, OldTeam receivingOldTeam) {
		DataManager.WarData.loadedWars.add(new War(declaringOldTeam, receivingOldTeam));
	}

	public static void removeWar(War war) {
		DataManager.WarData.loadedWars.remove(war);
	}
}