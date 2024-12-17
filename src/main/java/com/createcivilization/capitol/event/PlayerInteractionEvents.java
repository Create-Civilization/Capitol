package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerInteractionEvents {

	private PlayerInteractionEvents() {}

	@SubscribeEvent
	public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		var player = event.getEntity();
		player.sendSystemMessage(Component.literal("WiiU!"));
		if (!TeamUtils.getPermissionInCurrentChunk(player).canInteractEntities()) {
			player.sendSystemMessage(Component.literal("You do not have permission to interact with this entity in this chunk!"));
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		var player = event.getEntity();
		player.sendSystemMessage(Component.literal("WiiU!"));
		if (!TeamUtils.getPermissionInCurrentChunk(player).canBreakBlocks()) {
			player.sendSystemMessage(Component.literal("You do not have permission to interact with this block in this chunk!"));
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}
}