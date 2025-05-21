package com.createcivilization.capitol.util;

import net.minecraft.server.MinecraftServer;

public interface IChunkData {

	int getTakeOverProgress();

	void setTakeOverProgress(int i);

	void resetTakeOverProgress();

	void incrementTakeOverProgress();

	void decrementTakeOverProgress();

	void updateTakeOverProgress(MinecraftServer server);
}