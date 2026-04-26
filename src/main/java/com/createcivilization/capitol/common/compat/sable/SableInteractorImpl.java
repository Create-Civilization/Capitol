package com.createcivilization.capitol.common.compat.sable;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class SableInteractorImpl implements SableInteractor {

	@Override
	public UUID getPlayerSubLevelId(Player player) {
		SubLevelAccess access = Sable.HELPER.getTrackingSubLevel(player);
		return access == null ? null : access.getUniqueId();
	}

	@Override
	public void attachObservers(ServerLevel level) {
		SubLevelContainer container = SubLevelContainer.getContainer(level);
		if (container == null) return;
		container.addObserver(new SableObserver());
	}
}
