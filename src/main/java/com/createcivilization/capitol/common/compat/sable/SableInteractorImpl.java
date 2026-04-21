package com.createcivilization.capitol.common.compat.sable;

import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class SableInteractorImpl implements SableInteractor {

	@Override
	public UUID getPlayerSubLevelId(Player player) {
		SubLevelAccess access = EntitySubLevelUtil.getLastTrackingSubLevel(player);
		return access == null ? null : access.getUniqueId();
	}

	@Override
	public void attachObservers(ServerLevel level) {
		SubLevelContainer container = SubLevelContainer.getContainer(level);
		if (container == null) return;
		container.addObserver(new SableObserver());
	}

}
