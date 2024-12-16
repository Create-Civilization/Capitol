package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.world.InteractionResult;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Capitol.MOD_ID)
public class PlayerInteractionEvents {

	private PlayerInteractionEvents() {}

	@SubscribeEvent
	public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		if (TeamUtils.isInClaimedChunk(event.getEntity())) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}
}