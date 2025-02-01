package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
		Player player = event.getEntity();
		if (hasAdminPermission(player)) return;
		if (CapitolConfig.SERVER.debugLogs.get()) player.sendSystemMessage(Component.literal("onPlayerInteractEntity firing!"));
		cancelIfPlayerHasInsufficientPermission(event, TeamUtils.canPlayerNotDoInChunk(event.getPos(), player, "interactEntities"), "interact with entities");
	}

	/**
	 * Handles players breaking blocks.
	 */
	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		if (hasAdminPermission(player)) return;
		if (CapitolConfig.SERVER.debugLogs.get()) player.sendSystemMessage(Component.literal("onPlayerBreakBlock firing!"));
		cancelIfPlayerHasInsufficientPermission(event, TeamUtils.canPlayerNotDoInChunk(event.getPos(), player, "breakBlocks"), "break blocks");
	}

	/**
	 * Handles players trying to right-click blocks.
	 */
	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		if (hasAdminPermission(player)) return;
		if (CapitolConfig.SERVER.debugLogs.get()) player.sendSystemMessage(Component.literal("onPlayerRightClickBlock firing!"));
		Map<String, Boolean> permission = TeamUtils.getPermissionInChunk(event.getPos(), player);
		Item mainHandItem = player.getMainHandItem().getItem();
		Item offhandItem = player.getOffhandItem().getItem();
		if (mainHandItem instanceof BlockItem || offhandItem instanceof BlockItem || mainHandItem instanceof BucketItem || offhandItem instanceof BucketItem) onPlayerPlaceBlock(event, player, permission);
		else onPlayerInteractBlock(event, player, permission);
	}

	/**
	 * Handles players trying to place blocks.
	 */
	public static void onPlayerPlaceBlock(PlayerInteractEvent.RightClickBlock event, Player player, Map<String, Boolean> permission) {
		if (CapitolConfig.SERVER.debugLogs.get()) player.sendSystemMessage(Component.literal("onPlayerPlaceBlock firing!"));
		cancelIfPlayerHasInsufficientPermission(event, !permission.get("placeBlocks"), "place blocks");
	}

	/**
	 * Handles players trying to interact with blocks.
	 */
	public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, Player player, Map<String, Boolean> permission) {
		if (CapitolConfig.SERVER.debugLogs.get()) player.sendSystemMessage(Component.literal("onPlayerInteractBlock firing!"));
		cancelIfPlayerHasInsufficientPermission(event, !permission.get("interactBlocks"), "interact with blocks");
	}

	/**
	 * Handles players trying to use items.
	 */
	@SubscribeEvent
	@SuppressWarnings("resource")
	public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();
		if (hasAdminPermission(player)) return;
		if (CapitolConfig.SERVER.debugLogs.get()) player.sendSystemMessage(Component.literal("onPlayerUseItem firing!"));
		ItemStack stack = event.getItemStack();
		Level level = player.level();
		Item item = stack.getItem();
		ResourceLocation dimension = level.dimension().location();
		ChunkPos chunkPos = level.getChunk(event.getPos()).getPos();
		if (TeamUtils.isChildChunk(dimension, chunkPos)
			&&
			(
				stack.is(Items.ENDER_PEARL) ||
				item.getDescriptionId().replace("item.", "").replace(".", "").contains("boat") ||
				item instanceof BucketItem
			)
			&& !TeamUtils.getTeam(player).deepEquals(TeamUtils.getTeam(chunkPos, dimension))
		) cancelIfPlayerHasInsufficientPermission(event, true, "use boats, enderpearls or buckets");
		cancelIfPlayerHasInsufficientPermission(event, TeamUtils.canPlayerNotDoInChunk(event.getPos(), player, "useItems"), "use items");
	}

	public static void cancelIfPlayerHasInsufficientPermission(PlayerInteractEvent event, boolean cancelIfTrue, String details) {
		if (cancelIfTrue && event.getEntity() instanceof ServerPlayer player) {
			player.displayClientMessage(Component.literal("You do not have permission to " + details + " in this chunk!"), true);
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}

	public static boolean hasAdminPermission(Player player) {
		return player.getPersistentData().contains("capitolTeamsAdminMode") && player.getPersistentData().getBoolean("capitolTeamsAdminMode");
	}
}