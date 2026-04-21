package com.createcivilization.capitol.common.compat.sable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface SableInteractor {

	UUID getPlayerSubLevelId(Player player);

	void attachObservers(ServerLevel level);

}
