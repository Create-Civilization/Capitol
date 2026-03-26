package com.createcivilization.capitol.common.managers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class PermissionManager {

	private PermissionManager() {}



	//TODO: Needs fixing. I am to lazy rn to add all the methods needed

	public static boolean playerCanAccessChunk(Player player) {
//		ChunkPos chunkPos = player.chunkPosition();
//		Level playerLevel = player.level();
//		boolean isChunkClaimed = ClaimManager.isChunkClaimed(chunkPos, playerLevel);
//		if (isChunkClaimed) {
//			boolean hasPermissionInChunk = playerHasBypass(player) ||
//				ClaimManager.playerHasPermissionInChunk(player, chunkPos, playerLevel);
//			return playerHasBypass(player);
//		}

		return true;
	}

	public static boolean playerHasBypass(Player player) {
		return player.hasPermissions(4); // Is operator
	}
}
