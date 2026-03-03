package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.managers.PermissionManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerInteractionEvents {
	/**
	 * Handles players trying to interact with entities.
	 */
	@SubscribeEvent
	public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		Player player = event.getEntity();
		if (!PermissionManager.playerCanAccessChunk(player))
			setCancelled(event);
	}

	/**
	 * Handles players breaking blocks.
	 */
	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		if (!PermissionManager.playerCanAccessChunk(player))
			setCancelled(event);
	}

	/**
	 * Handles players trying to right-click blocks.
	 */
	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		if (!PermissionManager.playerCanAccessChunk(player))
			setCancelled(event);

		Item mainHandItem = player.getMainHandItem().getItem();
		Item offHandItem = player.getOffhandItem().getItem();
		boolean isBlockItem = mainHandItem instanceof BlockItem ||
			offHandItem instanceof BlockItem ||
			mainHandItem instanceof BucketItem ||
			offHandItem instanceof BucketItem;

		if (isBlockItem) onPlayerPlaceBlock(event, player);
		else onPlayerInteractBlock(event, player);
	}

	/**
	 * Handles players trying to place blocks.
	 */
	public static void onPlayerPlaceBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
		if (!PermissionManager.playerCanAccessChunk(player))
			setCancelled(event);
	}

	/**
	 * Handles players trying to interact with blocks.
	 */
	public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
		if (!PermissionManager.playerCanAccessChunk(player))
			setCancelled(event);
	}

	/**
	 * Handles players trying to use items.
	 */
	@SubscribeEvent
	public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();
		if (!PermissionManager.playerCanAccessChunk(player))
			setCancelled(event);
	}

	public static void setCancelled(PlayerInteractEvent event) {
		if (!(event instanceof ICancellableEvent cancellableEvent)) return;
		cancellableEvent.setCanceled(true);
	}
}
