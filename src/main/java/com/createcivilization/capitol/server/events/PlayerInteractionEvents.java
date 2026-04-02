package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.managers.PermissionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;

import net.minecraft.ChatFormatting;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerInteractionEvents {

	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.BREAK_BLOCKS, level, pos)) {
			setCancelled(event);
			sendActionbarMessage("You can't break blocks here!", player);
		}
	}

	@SubscribeEvent
	public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		Item mainHandItem = player.getMainHandItem().getItem();
		Item offHandItem = player.getOffhandItem().getItem();
		boolean isBlockItem = mainHandItem instanceof BlockItem ||
			offHandItem instanceof BlockItem ||
			mainHandItem instanceof BucketItem ||
			offHandItem instanceof BucketItem;

		if (isBlockItem) onPlayerPlaceBlock(event, player);
		else onPlayerInteractBlock(event, player);
	}

	public static void onPlayerPlaceBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.PLACE_BLOCKS, level, pos)) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			player.inventoryMenu.sendAllDataToRemote();
			sendActionbarMessage("You can't place blocks here!", player);
		}
	}

	public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();

		BlockState blockState = level.getBlockState(blockPos);
		if (blockState.getMenuProvider(level, blockPos) != null) {
			if (PermissionManager.playerHasPermission(player, Permission.OPEN_CONTAINERS, level, pos)) {
				return;
			}
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			sendActionbarMessage("You can't open containers here!", player);
			return;
		}

		if(blockState.isSignalSource()){
			if(!PermissionManager.playerHasPermission(player, Permission.INTERACT_REDSTONE, level, pos)){
				event.setCancellationResult(InteractionResult.FAIL);
				event.setCanceled(true);
				sendActionbarMessage("You can't interact with redstone here!", player);
				return;
			}
		}

		if (!PermissionManager.playerHasPermission(player, Permission.INTERACT_BLOCKS, level, pos)) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			sendActionbarMessage("You can't interact with this block!", player);
		}
	}

	@SubscribeEvent
	public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.USE_ITEMS, level, pos)) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			player.inventoryMenu.sendAllDataToRemote();
			sendActionbarMessage("You can't use items here!", player);
		}
	}

	@SubscribeEvent
	public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		Player player = event.getEntity();
		BlockPos blockPos = event.getPos();
		ChunkPos pos = new ChunkPos(blockPos);
		Level level = event.getLevel();
		if (!PermissionManager.playerHasPermission(player, Permission.INTERACT_ENTITIES, level, pos)) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			sendActionbarMessage("You can't interact with this entity!", player);
		}
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		Entity target = event.getTarget();
		BlockPos targetPos = target.getOnPos();
		ChunkPos pos = new ChunkPos(targetPos);
		Level level = event.getTarget().level();

		if (target instanceof Player) {
			if (!PermissionManager.playerHasPermission(player, Permission.PLAYER_ATTACK, level, pos)) {
				event.setCanceled(true);
				sendActionbarMessage("You can't attack players here!", player);
			}
			return;
		}

		if (target instanceof Monster) {
			if (!PermissionManager.playerHasPermission(player, Permission.KILL_HOSTILE, level, pos)) {
				event.setCanceled(true);
				sendActionbarMessage("You can't kill hostile mobs here!", player);
			}
			return;
		}

		if (!PermissionManager.playerHasPermission(player, Permission.KILL_ENTITIES, level, pos)) {
			event.setCanceled(true);
			sendActionbarMessage("You can't kill entities here!", player);
		}
	}

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

	@SubscribeEvent
	public static void onItemDrop(ItemTossEvent event) {
		Player player = event.getPlayer();
		BlockPos blockPos = player.getOnPos();
		ChunkPos chunkPos = new ChunkPos(blockPos);
		Level level = player.level();
		ItemStack itemStack = event.getEntity().getItem();

		if (!PermissionManager.playerHasPermission(player, Permission.TOSS_ITEMS, level, chunkPos)) {
			event.setCanceled(true);
			player.addItem(itemStack);
			sendActionbarMessage("You can't drop items here!", player);
		}
	}

	@SubscribeEvent
	public static void onFarmLandTrample(BlockEvent.FarmlandTrampleEvent event){
		if(event.getEntity() instanceof Player player){
			BlockPos pos = event.getPos();
			ChunkPos chunkPos = new ChunkPos(pos);
			Level level = event.getEntity().level();
			if(!PermissionManager.playerHasPermission(player, Permission.CROP_TRAMPLE, level, chunkPos)){
				event.setCanceled(true);
				sendActionbarMessage("You can't trample crops here!", player);
			}
		}
	}

	@SubscribeEvent
	public static void onFrostWalk(BlockEvent.EntityPlaceEvent event) {
		if (event.getEntity() instanceof Player player) {
			BlockPos pos = event.getPos();
			ChunkPos chunkPos = new ChunkPos(pos);
			Level level = player.level();
			if (event.getPlacedBlock().getBlock() instanceof FrostedIceBlock) {
				if (!PermissionManager.playerHasPermission(player, Permission.FROST_WALKING, level, chunkPos)) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPickupEvent(ItemEntityPickupEvent.Pre event) {
		Player player = event.getPlayer();
		BlockPos pos = event.getItemEntity().getOnPos();
		ChunkPos chunkPos = new ChunkPos(pos);
		Level level = event.getItemEntity().level();

		if (!PermissionManager.playerHasPermission(player, Permission.PICKUP_ITEMS, level, chunkPos)) {
			event.setCanPickup(TriState.FALSE);
			sendActionbarMessage("You can't pick up items here!", player);
			return;
		}

		ItemEntity item = event.getItemEntity();

		if (item.getPersistentData().getBoolean("MobDeathDrop")) {
			if (!PermissionManager.playerHasPermission(player, Permission.MOB_LOOT, level, chunkPos)) {
				event.setCanPickup(TriState.FALSE);
				sendActionbarMessage("You can't pick up mob loot here!", player);
				return;
			}
		}

		if (item.getPersistentData().getBoolean("DeathDrop")) {
			if (!PermissionManager.playerHasPermission(player, Permission.PLAYER_DEATH_LOOT, level, chunkPos)) {
				event.setCanPickup(TriState.FALSE);
				sendActionbarMessage("You can't pick up death loot here!", player);
			}
		}
	}

	@SubscribeEvent
	public static void onXpOrbPickUp(PlayerXpEvent.PickupXp event) {
		Player player = event.getEntity();
		BlockPos pos = event.getOrb().getOnPos();
		ChunkPos chunkPos = new ChunkPos(pos);
		Level level = event.getOrb().level();

		if (!PermissionManager.playerHasPermission(player, Permission.PICKUP_XP, level, chunkPos)) {
			event.setCanceled(true);
			sendActionbarMessage("You can't pick up XP here!", player);
		}
	}

	@SubscribeEvent
	public static void onPlayerChorusFruit(EntityTeleportEvent.ChorusFruit event) {
		if (event.getEntity() instanceof Player player) {
			BlockPos blockPos = player.getOnPos();
			ChunkPos chunkPos = new ChunkPos(blockPos);
			Level level = player.level();

			if (!PermissionManager.playerHasPermission(player, Permission.CHORUS_FRUIT_TELEPORT, level, chunkPos)) {
				event.setCanceled(true);
				sendActionbarMessage("You can't teleport here!", player);
			}
		}
	}

	@SubscribeEvent
	public static void onNetherPortalUse(EntityTravelToDimensionEvent event) {
		if (event.getDimension() != Level.NETHER) return;

		if (event.getEntity() instanceof Player player) {
			BlockPos blockPos = player.getOnPos();
			ChunkPos chunkPos = new ChunkPos(blockPos);
			Level level = player.level();

			if (!PermissionManager.playerHasPermission(player, Permission.USE_NETHER_PORTALS, level, chunkPos)) {
				event.setCanceled(true);
				sendActionbarMessage("You can't use portals here!", player);
			}
		}
	}

	public static void setCancelled(PlayerInteractEvent event) {
		if (!(event instanceof ICancellableEvent cancellableEvent)) return;
		cancellableEvent.setCanceled(true);
	}

	public static void sendActionbarMessage(String message, Player player) {
		player.displayClientMessage(
			Component.literal(message).withStyle(ChatFormatting.RED),
			true
		);
	}
}