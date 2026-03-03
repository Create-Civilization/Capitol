package com.createcivilization.capitol.common.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Team {
	private static final List<WeakReference<ChunkPos>> chunks = new ArrayList<>();
	private static final List<Player> players = new ArrayList<>();

	public boolean hasChunkAt(ChunkPos chunkPos) {
		return chunks.contains(chunkPos);
	}

	public boolean hasPlayer(Player player) {
		return players.contains(player);
	}
}
