package com.createcivilization.capitol.common.modules.database;

import com.createcivilization.capitol.common.data.Team;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.util.LinkedList;
import java.util.List;

public class DummyDatabase extends Database {

	private static final List<Team> teams = new LinkedList<>();

	@Override
	public boolean hasChunkAt(ChunkPos chunkPos) {
		return teams.stream().anyMatch(team -> team.hasChunkAt(chunkPos));
	}

	@Override
	public boolean hasPermissionInChunk(Player player, ChunkPos chunkPos) {
		Team chunkOwner = getChunkOwner(chunkPos);
		return chunkOwner != null && chunkOwner.hasPlayer(player);
	}

	@Override
	public Team getChunkOwner(ChunkPos chunkPos) {
		for (Team team : teams) {
			if (!team.hasChunkAt(chunkPos))
				continue;
			return team;
		}
		return null;
	}
}
