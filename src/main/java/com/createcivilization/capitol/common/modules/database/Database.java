package com.createcivilization.capitol.common.modules.database;

import com.createcivilization.capitol.common.data.Team;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public abstract class Database {

	public abstract boolean hasChunkAt(ChunkPos chunkPos, Level level);
	public abstract boolean hasPermissionInChunk(Player player, ChunkPos chunkPos, Level level);
	public abstract Team getChunkOwner(ChunkPos chunkPos, Level level);
}
