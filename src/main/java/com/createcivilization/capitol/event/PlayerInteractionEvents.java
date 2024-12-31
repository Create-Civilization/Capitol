package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.util.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Map;

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
		if (Config.debugLogs.getOrThrow()) player.sendSystemMessage(Component.literal("onPlayerInteractEntity firing!"));
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).get("interactEntities"), "interact with entities");
	}

	/**
	 * Handles players breaking blocks.
	 */
	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		var player = event.getEntity();
		if (Config.debugLogs.getOrThrow()) player.sendSystemMessage(Component.literal("onPlayerBreakBlock firing!"));
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).get("breakBlocks"), "break blocks");
	}

	/**
	 * Handles players trying to right-click blocks.
	 */
	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		var player = event.getEntity();
		if (Config.debugLogs.getOrThrow()) player.sendSystemMessage(Component.literal("onPlayerRightClickBlock firing!"));
		Map<String, Boolean> permission = TeamUtils.getPermissionInChunk(event.getPos(), player);
		var mainHandItem = player.getMainHandItem().getItem();
		var offhandItem = player.getOffhandItem().getItem();
		if (mainHandItem instanceof BlockItem || offhandItem instanceof BlockItem || mainHandItem instanceof BucketItem || offhandItem instanceof BucketItem) onPlayerPlaceBlock(event, player, permission);
		else onPlayerInteractBlock(event, player, permission);
	}

	/**
	 * Handles players trying to place blocks.
	 */
	public static void onPlayerPlaceBlock(PlayerInteractEvent.RightClickBlock event, Player player, Map<String, Boolean> permission) {
		if (Config.debugLogs.getOrThrow()) player.sendSystemMessage(Component.literal("onPlayerPlaceBlock firing!"));
		cancelIfHasInsufficientPermission(event, !permission.get("placeBlocks"), "place blocks");
	}

	/**
	 * Handles players trying to interact with blocks.
	 */
	public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, Player player, Map<String, Boolean> permission) {
		if (Config.debugLogs.getOrThrow()) player.sendSystemMessage(Component.literal("onPlayerInteractBlock firing!"));
		cancelIfHasInsufficientPermission(event, !permission.get("interactBlocks"), "interact with blocks");
	}

	/**
	 * Handles players trying to use items.
	 */
	@SubscribeEvent
	@SuppressWarnings("resource")
	public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
		var player = event.getEntity();
		if (Config.debugLogs.getOrThrow()) player.sendSystemMessage(Component.literal("onPlayerUseItem firing!"));
		var stack = event.getItemStack();
		var level = player.level();
		var item = stack.getItem();
		if (TeamUtils.isClaimedChunk(level.dimension().location(), level.getChunk(event.getPos()).getPos())
			&&
			(
				stack.is(Items.ENDER_PEARL) ||
				item.getDescriptionId().replace("item.", "").replace(".", "").contains("boat") ||
				item instanceof BucketItem
			)
		) cancelIfHasInsufficientPermission(event, true, "use boats, enderpearls or buckets");
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).get("useItems"), "use items");
	}

	/**
	 * Utility method to cancel the event if the player has insufficient permissions in the chunk.
	 */
	public static void cancelIfHasInsufficientPermission(PlayerInteractEvent event, Boolean cancelIfTrue, String details) {
		if (cancelIfTrue) {
			event.getEntity().sendSystemMessage(Component.literal("You do not have permission to " + details + " in this chunk!"));
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}
}