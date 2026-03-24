package com.createcivilization.capitol.common.modules.database;

import com.createcivilization.capitol.common.data.Team;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.LinkedList;
import java.util.List;

public class DummyDatabase extends Database {

	private static final List<Team> teams = new LinkedList<>();

	@Override
	public boolean hasChunkAt(ChunkPos chunkPos, Level level) {
		return teams.stream().anyMatch(team -> team.hasChunkAt(chunkPos));
	}

	@Override
	public boolean hasPermissionInChunk(Player player, ChunkPos chunkPos, Level level) {
		Team chunkOwner = getChunkOwner(chunkPos, level);
		return chunkOwner != null && chunkOwner.hasPlayer(player);
	}

	@Override
	public Team getChunkOwner(ChunkPos chunkPos, Level level) {
		for (Team team : teams) {
			if (!team.hasChunkAt(chunkPos))
				continue;
			return team;
		}
		return null;
	}
}
