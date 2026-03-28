package com.createcivilization.capitol.common.managers;

import com.createcivilization.capitol.common.data.Permission;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ClaimManager {

	public static boolean isChunkClaimed(ChunkPos chunkPos, Level level) {
		return DatabaseManager.database.hasChunkAt(chunkPos, level);
	}

	public static boolean playerHasPermissionInChunk(Player player, ChunkPos chunkPos, Level level, Permission perm) {
		int playerPerms = DatabaseManager.database.getPermissionInChunk(player, chunkPos, level);
		return perm.hasPermission(playerPerms);
	}
}
