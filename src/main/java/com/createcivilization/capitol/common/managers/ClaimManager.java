package com.createcivilization.capitol.common.managers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class ClaimManager {

	public static boolean isChunkClaimed(ChunkPos chunkPos) {
		return DatabaseManager.database.hasChunkAt(chunkPos);
	}

	public static boolean playerHasPermissionInChunk(Player player, ChunkPos chunkPos) {
		return DatabaseManager.database.hasPermissionInChunk(player, chunkPos);
	}
}
