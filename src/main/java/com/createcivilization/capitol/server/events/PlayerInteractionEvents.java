package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.managers.PermissionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.awt.*;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerInteractionEvents {

	@SubscribeEvent
	public static void onPlayerDrops(LivingDropsEvent event) {
		if (event.getEntity() instanceof Player) {
			for (ItemEntity itemEntity : event.getDrops()) {
				itemEntity.getPersistentData().putBoolean("DeathDrop", true);
			}
		}

		if (event.getEntity() instanceof Mob) {
			for (ItemEntity itemEntity : event.getDrops()) {
				itemEntity.getPersistentData().putBoolean("MobDeathDrop", true);
			}
		}
	}

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
			sendActionbarMessage("You can't interact with this entity!", Color.RED, player);
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
				sendActionbarMessage("You can't attack players here!", Color.RED, player);
			}
			return;
		}

		//Assume hostile
		if(target instanceof Monster && !PermissionManager.playerHasPermission(player, Permission.KILL_HOSTILE, level, pos)){
			event.setCanceled(true);
			sendActionbarMessage("You can't kill hostile mobs here!", Color.RED, player);
			return;
		}

		//Assume non hostile
		if(!PermissionManager.playerHasPermission(player, Permission.KILL_ENTITIES, level, pos)){
			event.setCanceled(true);
			sendActionbarMessage("You can't kill entities here!", Color.RED, player);
		}
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
		if (!PermissionManager.playerHasPermission(player, Permission.BREAK_BLOCKS, level, pos)) {
			setCancelled(event);
			sendActionbarMessage("You can't break blocks here!", Color.RED, player);
		}
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

	@SubscribeEvent
	public static void onPickupEvent(ItemEntityPickupEvent.Pre event){
		Player player = event.getPlayer();
		BlockPos pos = event.getItemEntity().getOnPos();
		ChunkPos chunkPos = new ChunkPos(pos);
		Level level = event.getItemEntity().level();

		if(!PermissionManager.playerHasPermission(player, Permission.PICKUP_ITEMS, level, chunkPos)){
			event.setCanPickup(TriState.FALSE);
			return;
		}

		ItemEntity item = event.getItemEntity();

		if(item.getPersistentData().getBoolean("MobDeathDrop")){
			if(!PermissionManager.playerHasPermission(player, Permission.MOB_LOOT, level, chunkPos)){
				event.setCanPickup(TriState.FALSE);
				return;
			}
		}

		if(item.getPersistentData().getBoolean("DeathDrop")){
			if(!PermissionManager.playerHasPermission(player, Permission.PLAYER_DEATH_LOOT, level, chunkPos)){
				event.setCanPickup(TriState.FALSE);
				return;
			}
		}

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
			sendActionbarMessage("You can't place blocks here!", Color.RED, player);
		}
	}

	/**
	 * Handles players trying to interact with blocks.
	 */
	public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();

		BlockState blockState = level.getBlockState(blockPos);
		if(blockState.getMenuProvider(level, blockPos) != null){
			if(PermissionManager.playerHasPermission(player, Permission.OPEN_CONTAINERS, level, pos)){
				return;
			}
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			sendActionbarMessage("You can't open containers here!", Color.RED, player);
			return;
		}

		if (!PermissionManager.playerHasPermission(player, Permission.INTERACT_BLOCKS, level, pos)){
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			sendActionbarMessage("You can't interact with this block!", Color.RED, player);
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
			sendActionbarMessage("You can't use items here!", Color.RED, player);
		}
	}

	public static void setCancelled(PlayerInteractEvent event) {
		if (!(event instanceof ICancellableEvent cancellableEvent)) return;
		cancellableEvent.setCanceled(true);
	}


	public static void sendActionbarMessage(String message, Color color, Player player) {
		player.displayClientMessage(
			Component.literal(message).withStyle(style -> style.withColor(color.getRGB() & 0xFFFFFF)),
			true
		);
	}
}
