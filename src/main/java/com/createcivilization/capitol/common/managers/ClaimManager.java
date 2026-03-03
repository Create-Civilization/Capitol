package com.createcivilization.capitol.common.managers;

import com.createcivilization.capitol.common.modules.database.Database;
import com.createcivilization.capitol.common.modules.database.DummyDatabase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class ClaimManager {
	public static Database database = new DummyDatabase();

	public static boolean isChunkClaimed(ChunkPos chunkPos) {
		return database.hasChunkAt(chunkPos);
	}

	public static boolean playerHasPermissionInChunk(Player player, ChunkPos chunkPos) {
		return database.hasPermissionInChunk(player, chunkPos);
	}
}
