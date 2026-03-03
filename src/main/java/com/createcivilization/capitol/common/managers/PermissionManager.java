package com.createcivilization.capitol.common.managers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class PermissionManager {

	private PermissionManager() {}

	public static boolean playerCanAccessChunk(Player player) {
		ChunkPos chunkPos = player.chunkPosition();
		boolean isChunkClaimed = ClaimManager.isChunkClaimed(chunkPos);
		if (isChunkClaimed) {
			boolean hasPermissionInChunk = playerHasBypass(player) ||
				ClaimManager.playerHasPermissionInChunk(player, chunkPos);
			return playerHasBypass(player);
		}

		return true;
	}

	public static boolean playerHasBypass(Player player) {
		return player.hasPermissions(4); // Is operator
	}
}
