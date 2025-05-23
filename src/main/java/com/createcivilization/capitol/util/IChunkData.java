package com.createcivilization.capitol.util;

import net.minecraft.server.MinecraftServer;

public interface IChunkData {

	int getTakeOverProgress();

	void setTakeOverProgress(int i);

	void resetTakeOverProgress();

	void incrementTakeOverProgress(int modifier);

	void decrementTakeOverProgress(int modifier);

	void updateTakeOverProgress(MinecraftServer server);
}