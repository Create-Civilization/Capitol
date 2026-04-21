package com.createcivilization.capitol.common.compat.sable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.util.UUID;

public class SableCompat {

	public static final boolean LOADED = ModList.get().isLoaded("sable");

	public static final SableInteractor INTERACTOR = LOADED ? new SableInteractorImpl() : new SableInteractorDummy();

	public static UUID getPlayerSubLevelId(Player player) {
		return INTERACTOR.getPlayerSubLevelId(player);
	}

	public static void attachObservers(ServerLevel level) {
		INTERACTOR.attachObservers(level);
	}

}
