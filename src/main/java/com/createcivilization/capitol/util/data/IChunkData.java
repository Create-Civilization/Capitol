package com.createcivilization.capitol.util.data;

import net.minecraft.server.MinecraftServer;

public interface IChunkData {

	int getTakeOverProgress();

	void setTakeOverProgress(Number i);

	void resetTakeOverProgress();

	void incrementTakeOverProgress(Number modifier);

	void decrementTakeOverProgress(Number modifier);

	void updateTakeOverProgress(MinecraftServer server);
}