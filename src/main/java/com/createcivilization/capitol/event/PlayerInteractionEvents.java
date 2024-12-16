package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Capitol.MOD_ID)
public class PlayerInteractionEvents {

	private PlayerInteractionEvents() {}

	@SubscribeEvent
	public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		var player = event.getEntity();
		if (!TeamUtils.getPermissionInCurrentChunk(player).canInteract()) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			player.sendSystemMessage(Component.literal("You do not have permission to interact with this entity in this chunk!"));
		}
	}
}