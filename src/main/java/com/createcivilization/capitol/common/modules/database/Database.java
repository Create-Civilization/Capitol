package com.createcivilization.capitol.common.modules.database;

import com.createcivilization.capitol.common.data.Team;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public abstract class Database {

	public abstract boolean hasChunkAt(ChunkPos chunkPos);
	public abstract boolean hasPermissionInChunk(Player player, ChunkPos chunkPos);
	public abstract Team getChunkOwner(ChunkPos chunkPos);
}
