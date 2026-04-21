package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.compat.sable.SableCompat;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = Capitol.MOD_ID)
public class SubLevelEvents {

	@SubscribeEvent
	public static void onLevelLoad(LevelEvent.Load event) {
		if (!SableCompat.LOADED) return;
		if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
		SableCompat.attachObservers(serverLevel);
	}

}
