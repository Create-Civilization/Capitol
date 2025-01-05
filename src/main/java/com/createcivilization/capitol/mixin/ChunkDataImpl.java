package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.team.War;
import com.createcivilization.capitol.util.*;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;

@Mixin(ChunkAccess.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class ChunkDataImpl implements IChunkData {

	// Inject information into relevant chunks

	@Shadow
	@Nullable
	public abstract LevelAccessor getWorldForge();

	// Represents a % of taken over progress
	@Unique
	private int takeOverProgress = 0;

	@Override
	public int getTakeOverProgress() {
		return this.takeOverProgress;
	}

	@Override
	public void setTakeOverProgress(int i) {
		this.takeOverProgress = i;
	}

	@Override
	public void updateTakeOverProgress() {
		for (War war : TeamUtils.wars) {

		}
	}
}