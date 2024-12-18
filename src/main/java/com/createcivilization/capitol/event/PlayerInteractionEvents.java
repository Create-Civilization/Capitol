package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.util.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Class to handle events related to player interaction in chunks.<br>
 * Events will be cancelled if the player is in a claimed chunk with no permissions or is interacting with a claimed chunk with no permissions.
 */
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
		player.sendSystemMessage(Component.literal("onPlayerInteractEntity firing!"));
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).canInteractWithEntities(), "interact with entities");
	}

	/**
	 * Handles players breaking blocks.
	 */
	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		var player = event.getEntity();
		player.sendSystemMessage(Component.literal("onPlayerBreakBlock firing!"));
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).canBreakBlocks(), "break blocks");
	}

	/**
	 * Handles players trying to right-click blocks.
	 */
	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		var player = event.getEntity();
		player.sendSystemMessage(Component.literal("onPlayerRightClickBlock firing!"));
		Permission permission = TeamUtils.getPermissionInChunk(event.getPos(), player);
		if (player.getMainHandItem().getItem() instanceof BlockItem || player.getOffhandItem().getItem() instanceof BlockItem) onPlayerPlaceBlock(event, player, permission);
		else onPlayerInteractBlock(event, player, permission);
	}

	/**
	 * Handles players trying to place blocks.
	 */
	public static void onPlayerPlaceBlock(PlayerInteractEvent.RightClickBlock event, Player player, Permission permission) {
		player.sendSystemMessage(Component.literal("onPlayerPlaceBlock firing!"));
		cancelIfHasInsufficientPermission(event, !permission.canPlaceBlocks(), "place blocks");
	}

	/**
	 * Handles players trying to interact with blocks.
	 */
	public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, Player player, Permission permission) {
		player.sendSystemMessage(Component.literal("onPlayerInteractBlock firing!"));
		cancelIfHasInsufficientPermission(event, !permission.canInteractBlocks(), "interact with blocks");
	}

	/**
	 * Handles players trying to use items.
	 */
	@SubscribeEvent
	@SuppressWarnings("resource")
	public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
		var player = event.getEntity();
		player.sendSystemMessage(Component.literal("onPlayerUseItem firing!"));
		var stack = event.getItemStack();
		if (TeamUtils.isClaimedChunk(player.level().getChunk(event.getPos()).getPos()) && (stack.is(Items.ENDER_PEARL) || stack.getItem().getDescriptionId().replace("item.", "").replace(".", "").contains("boat"))) cancelIfHasInsufficientPermission(event, true, "use boats or enderpearls");
		cancelIfHasInsufficientPermission(event, !TeamUtils.getPermissionInChunk(event.getPos(), player).canUseItems(), "use items");
	}

	/**
	 * Utility method to cancel the event if the player has insufficient permissions in the chunk.
	 */
	public static void cancelIfHasInsufficientPermission(PlayerInteractEvent event, boolean cancelIfTrue, String details) {
		if (cancelIfTrue) {
			event.getEntity().sendSystemMessage(Component.literal("You do not have permission to " + details + " in this chunk!"));
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}
}