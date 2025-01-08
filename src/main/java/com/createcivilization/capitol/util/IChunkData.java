package com.createcivilization.capitol.util;

import net.minecraft.server.MinecraftServer;

public interface IChunkData {

	float getTakeOverProgress();

	void setTakeOverProgress(float i);

	void incrementTakeOverProgress();

	void decrementTakeOverProgress();

	void updateTakeOverProgress(MinecraftServer server);
}