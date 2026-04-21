package com.createcivilization.capitol.common.compat.sable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class SableInteractorDummy implements SableInteractor {

	@Override
	public UUID getPlayerSubLevelId(Player player) {
		return null;
	}

	@Override
	public void attachObservers(ServerLevel level) {}

}
