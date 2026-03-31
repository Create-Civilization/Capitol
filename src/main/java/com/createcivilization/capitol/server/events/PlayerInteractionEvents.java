package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.managers.PermissionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerInteractionEvents {
	/**
	 * Handles players trying to interact with entities.
	 */
	@SubscribeEvent
	public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		Player player = event.getEntity();
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.INTERACT_ENTITIES, level, pos)) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event){
		Player player = event.getEntity();
		Entity target = event.getTarget();
		BlockPos targetPos = target.getOnPos();
		ChunkPos pos = new ChunkPos(targetPos);
		Level level = event.getTarget().level();

		//Is it player on player violence
		if(target instanceof Player){
			if(!PermissionManager.playerHasPermission(player, Permission.PLAYER_ATTACK, level, pos)){
				event.setCanceled(true);
				Capitol.LOGGER.info("PLAYER!!!");
			}
			return;
		}

		//Assume hostile
		if(target instanceof Monster && !PermissionManager.playerHasPermission(player, Permission.KILL_HOSTILE, level, pos)){
			event.setCanceled(true);
			Capitol.LOGGER.info("HOSTILE");
			return;
		}

		//Assume non hostile
		if(!PermissionManager.playerHasPermission(player, Permission.KILL_ENTITIES, level, pos)){
			event.setCanceled(true);
			Capitol.LOGGER.info("NON HOSTILE");
		}

		Capitol.LOGGER.info("Passed Attack Event");
	}

	/**
	 * Handles players breaking blocks.
	 */
	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.BREAK_BLOCKS, level, pos))
			setCancelled(event);
	}

	/**
	 * Handles players trying to right-click blocks.
	 */
	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
//		if (!PermissionManager.playerCanAccessChunk(player))
//			setCancelled(event);
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
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.PLACE_BLOCKS, level, pos)){
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			player.inventoryMenu.sendAllDataToRemote();
		}
	}

	/**
	 * Handles players trying to interact with blocks.
	 */
	public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.INTERACT_BLOCKS, level, pos)){
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
		}
	}

	/**
	 * Handles players trying to use items.
	 */
	@SubscribeEvent
	public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.USE_ITEMS, level, pos)){
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			player.inventoryMenu.sendAllDataToRemote();
		}
	}

	public static void setCancelled(PlayerInteractEvent event) {
		if (!(event instanceof ICancellableEvent cancellableEvent)) return;
		cancellableEvent.setCanceled(true);
	}
}
