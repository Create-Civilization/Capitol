package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.util.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Class to handle events related to player interaction in chunks.<br>
 * Events will be cancelled if the player is in a claimed chunk with no permissions or is interacting with a claimed chunk with no permissions.
 */
@SuppressWarnings("TypeMayBeWeakened")
@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerInteractionEvents {

	/**
	 * Not to be instanced.
	 */
	private PlayerInteractionEvents() {}

	/**
	 * Handles players trying to interact with entities.
	 */
	@SubscribeEvent
	public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		var player = event.getEntity();
		if (Config.debugLogs.getOrThrow()) player.sendMessage(Text.literal("onPlayerInteractEntity firing!"));
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).interactEntities(), "interact with entities");
	}

	/**
	 * Handles players breaking blocks.
	 */
	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		var player = event.getEntity();
		if (Config.debugLogs.getOrThrow()) player.sendMessage(Text.literal("onPlayerBreakBlock firing!"));
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).breakBlocks(), "break blocks");
	}

	/**
	 * Handles players trying to right-click blocks.
	 */
	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		var player = event.getEntity();
		if (Config.debugLogs.getOrThrow()) player.sendMessage(Text.literal("onPlayerRightClickBlock firing!"));
		Permission permission = TeamUtils.getPermissionInChunk(event.getPos(), player);
		var mainHandItem = player.getMainHandStack().getItem();
		var offhandItem = player.getOffHandStack().getItem();
		if (mainHandItem instanceof BlockItem || offhandItem instanceof BlockItem || mainHandItem instanceof BucketItem || offhandItem instanceof BucketItem) onPlayerPlaceBlock(event, player, permission);
		else onPlayerInteractBlock(event, player, permission);
	}

	/**
	 * Handles players trying to place blocks.
	 */
	public static void onPlayerPlaceBlock(PlayerInteractEvent.RightClickBlock event, PlayerEntity player, Permission permission) {
		if (Config.debugLogs.getOrThrow()) player.sendMessage(Text.literal("onPlayerPlaceBlock firing!"));
		cancelIfHasInsufficientPermission(event, !permission.placeBlocks(), "place blocks");
	}

	/**
	 * Handles players trying to interact with blocks.
	 */
	public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, PlayerEntity player, Permission permission) {
		if (Config.debugLogs.getOrThrow()) player.sendMessage(Text.literal("onPlayerInteractBlock firing!"));
		cancelIfHasInsufficientPermission(event, !permission.interactBlocks(), "interact with blocks");
	}

	/**
	 * Handles players trying to use items.
	 */
	@SubscribeEvent
	public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
		var player = event.getEntity();
		if (Config.debugLogs.getOrThrow()) player.sendMessage(Text.literal("onPlayerUseItem firing!"));
		var stack = event.getItemStack();
		var level = player.getWorld();
		var item = stack.getItem();
		if (TeamUtils.isClaimedChunk(level.getRegistryKey().getValue(), level.getChunk(event.getPos()).getPos())
			&&
			(
				stack.isOf(Items.ENDER_PEARL) ||
				item.getTranslationKey().replace("item.", "").replace(".", "").contains("boat") ||
				item instanceof BucketItem
			)
		) cancelIfHasInsufficientPermission(event, true, "use boats, enderpearls or buckets");
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).useItems(), "use items");
	}

	/**
	 * Utility method to cancel the event if the player has insufficient permissions in the chunk.
	 */
	public static void cancelIfHasInsufficientPermission(PlayerInteractEvent event, boolean cancelIfTrue, String details) {
		if (cancelIfTrue) {
			event.getEntity().sendMessage(Text.literal("You do not have permission to " + details + " in this chunk!"));
			event.setCancellationResult(ActionResult.FAIL);
			event.setCanceled(true);
		}
	}
}