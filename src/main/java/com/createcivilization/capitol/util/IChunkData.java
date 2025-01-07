package com.createcivilization.capitol.util;

import net.minecraft.server.MinecraftServer;

public interface IChunkData {

	float getTakeOverProgress();

	void setTakeOverProgress(float i);

	void incrementTakeOverProgress();

	void updateTakeOverProgress(MinecraftServer server);
}