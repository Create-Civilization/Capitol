package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol .Capitol;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.managers.ProtectionManager.Result;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.FrostedIceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Objects;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class PlayerInteractionEvents {

	@SubscribeEvent
	public static void onPlayerBreakBlock(PlayerInteractEvent.LeftClickBlock event) {
		Player player = event.getEntity();
		BlockPos blockPos = event.getPos();
		Level level = event.getLevel();

		// FIXME: This code was clearly copied over from onPlayerPlaceBlock, but needs to actually do _break_ block logic.
//		SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, blockPos);
//		if (subLevelAccess != null) {
//			if (ProtectionManager.checkBlockPlace(player, level.getBlockState(blockPos).getBlock(), subLevelAccess) == Result.DENY) {
//				event.setCanceled(true);
//				sendDenied("You can't place blocks here!", player);
//			}
//			return;
//		}

		if (ProtectionManager.checkBlockBreak(player, level.getBlockState(blockPos).getBlock(), level, new ChunkPos(blockPos)) == Result.DENY) {
			event.setCanceled(true);
			sendDenied("You can't break blocks here!", player);
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

	private static void onPlayerPlaceBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
		BlockPos blockPos = event.getPos();
		Level level = event.getLevel();

		//Sable stuff
		SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, blockPos);
		if (subLevelAccess != null) {
			if (ProtectionManager.checkBlockPlace(player, level.getBlockState(blockPos).getBlock(), subLevelAccess) == Result.DENY) {
				event.setCancellationResult(InteractionResult.FAIL);
				event.setCanceled(true);
				player.inventoryMenu.sendAllDataToRemote();
				sendDenied("You can't place blocks here!", player);
			}
			return;
		}

		if (ProtectionManager.checkBlockPlace(player, level.getBlockState(blockPos).getBlock(), level, new ChunkPos(blockPos)) == Result.DENY) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			player.inventoryMenu.sendAllDataToRemote();
			sendDenied("You can't place blocks here!", player);
		}


	}

	private static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
		BlockPos pos = event.getPos();
		Level level = event.getLevel();
		BlockState state = level.getBlockState(pos);
		ChunkPos chunkPos = new ChunkPos(pos);

		if (handleContainer(event, player, level, state, pos, chunkPos)) return;
		if (handleRedstone(event, player, level, state, pos, chunkPos)) return;
		handleGenericBlock(event, player, level, state, pos, chunkPos);
	}

	private static boolean handleContainer(PlayerInteractEvent.RightClickBlock event, Player player, Level level, BlockState state, BlockPos pos, ChunkPos chunkPos) {
		if (state.getMenuProvider(level, pos) == null) return false;

		//Sable stuff
		SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);
		if (subLevelAccess != null) {
			return ProtectionManager.checkContainerOpen(player, level.getBlockState(pos).getBlock(), subLevelAccess) != Result.DENY;
		}

		if (ProtectionManager.checkContainerOpen(player, state.getBlock(), level, chunkPos) != Result.DENY) {
			return true;
		}

		event.setCancellationResult(InteractionResult.FAIL);
		event.setCanceled(true);
		sendDenied("You can't open containers here!", player);
		return true;
	}

	private static boolean handleRedstone(PlayerInteractEvent.RightClickBlock event, Player player, Level level, BlockState state, BlockPos pos, ChunkPos chunkPos) {
		if (!state.isSignalSource()) return false;

		SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);
		if (subLevelAccess != null) {
			return ProtectionManager.checkRedstoneInteract(player, level.getBlockState(pos).getBlock(), subLevelAccess) != Result.DENY;
		}

		return ProtectionManager.checkRedstoneInteract(player, state.getBlock(), level, chunkPos) == Result.DENY;
	}

	private static void handleGenericBlock(PlayerInteractEvent.RightClickBlock event, Player player, Level level, BlockState state, BlockPos pos, ChunkPos chunkPos) {
		SubLevelAccess subLevelAccess = SableCompanion.INSTANCE.getContaining(level, pos);
		if (subLevelAccess != null) {
			if (ProtectionManager.checkBlockInteract(player, state.getBlock(), subLevelAccess) == Result.DENY) {
				genericBlockFailed(event, player);
			}
			return;
		}

		if (ProtectionManager.checkBlockInteract(player, state.getBlock(), level, chunkPos) == Result.DENY) {
			genericBlockFailed(event, player);
		}
	}

	private static void genericBlockFailed(PlayerInteractEvent.RightClickBlock event, Player player) {
		event.setCancellationResult(InteractionResult.FAIL);
		event.setCanceled(true);
		sendDenied("You can't interact with this block!", player);
		if (player instanceof ServerPlayer serverPlayer) {
			Objects.requireNonNull(serverPlayer.getServer(), "serverPlayer.getServer()")
				.execute(serverPlayer::closeContainer);
		}
	}

	@SubscribeEvent
	public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
		Player player = event.getEntity();
		if (ProtectionManager.checkItemUse(player, player.getMainHandItem().getItem(), event.getLevel(), new ChunkPos(event.getPos())) == Result.DENY) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			player.inventoryMenu.sendAllDataToRemote();
			sendDenied("You can't use items here!", player);
		}
	}

	@SubscribeEvent
	public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
		Player player = event.getEntity();
		Entity target = event.getTarget();
		if (ProtectionManager.checkEntityAction(player, target, Permission.INTERACT_ENTITIES, target.level(), new ChunkPos(target.getOnPos())) == Result.DENY) {
			event.setCancellationResult(InteractionResult.FAIL);
			event.setCanceled(true);
			sendDenied("You can't interact with this entity!", player);
		}
	}

	@SubscribeEvent
	public static void onAttackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		Entity target = event.getTarget();
		ChunkPos pos = new ChunkPos(target.getOnPos());
		Level level = target.level();

		if (target instanceof Player) {
			if (ProtectionManager.checkEntityAction(player, target, Permission.ATTACK_PLAYER, level, pos) == Result.DENY) {
				event.setCanceled(true);
				sendDenied("You can't attack players here!", player);
			}
			return;
		}

		Permission perm = target instanceof Monster ? Permission.ATTACK_HOSTILE : Permission.ATTACK_PASSIVE;
		String msg = target instanceof Monster ? "You can't attack hostile mobs here!" : "You can't attack passive mobs here!";

		if (ProtectionManager.checkEntityAction(player, target, perm, level, pos) == Result.DENY) {
			event.setCanceled(true);
			sendDenied(msg, player);
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
		ItemStack itemStack = event.getEntity().getItem();
		if (ProtectionManager.checkPlayerAction(player, Permission.DROP_ITEMS, player.level(), new ChunkPos(player.getOnPos())) == Result.DENY) {
			event.setCanceled(true);
			player.addItem(itemStack);
			sendDenied("You can't drop items here!", player);
		}
	}

	@SubscribeEvent
	public static void onFarmLandTrample(BlockEvent.FarmlandTrampleEvent event) {
		if (event.getEntity() instanceof Player player) {
			if (ProtectionManager.checkPlayerAction(player, Permission.CROP_TRAMPLE, player.level(), new ChunkPos(event.getPos())) == Result.DENY) {
				event.setCanceled(true);
				sendDenied("You can't trample crops here!", player);
			}
		}
	}

	@SubscribeEvent
	public static void onFrostWalk(BlockEvent.EntityPlaceEvent event) {
		if (event.getEntity() instanceof Player player) {
			if (event.getPlacedBlock().getBlock() instanceof FrostedIceBlock) {
				if (ProtectionManager.checkPlayerAction(player, Permission.FROST_WALKING, player.level(), new ChunkPos(event.getPos())) == Result.DENY) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPickupEvent(ItemEntityPickupEvent.Pre event) {
		Player player = event.getPlayer();
		ChunkPos chunkPos = new ChunkPos(event.getItemEntity().getOnPos());
		Level level = event.getItemEntity().level();

		if (ProtectionManager.checkPlayerAction(player, Permission.PICKUP_ITEMS, level, chunkPos) == Result.DENY) {
			event.setCanPickup(TriState.FALSE);
			sendDenied("You can't pick up items here!", player);
			return;
		}

		ItemEntity item = event.getItemEntity();
		if (item.getPersistentData().getBoolean("MobDeathDrop")) {
			if (ProtectionManager.checkPlayerAction(player, Permission.MOB_LOOT, level, chunkPos) == Result.DENY) {
				event.setCanPickup(TriState.FALSE);
				sendDenied("You can't pick up mob loot here!", player);
				return;
			}
		}
		if (item.getPersistentData().getBoolean("DeathDrop")) {
			if (ProtectionManager.checkPlayerAction(player, Permission.PLAYER_DEATH_LOOT, level, chunkPos) == Result.DENY) {
				event.setCanPickup(TriState.FALSE);
				sendDenied("You can't pick up death loot here!", player);
			}
		}
	}

	@SubscribeEvent
	public static void onXpOrbPickUp(PlayerXpEvent.PickupXp event) {
		Player player = event.getEntity();
		if (ProtectionManager.checkPlayerAction(player, Permission.PICKUP_XP, event.getOrb().level(), new ChunkPos(event.getOrb().getOnPos())) == Result.DENY) {
			event.setCanceled(true);
			sendDenied("You can't pick up XP here!", player);
		}
	}

	@SubscribeEvent
	public static void onPlayerChorusFruit(EntityTeleportEvent.ChorusFruit event) {
		if (event.getEntity() instanceof Player player) {
			if (ProtectionManager.checkPlayerAction(player, Permission.CHORUS_FRUIT_TELEPORT, player.level(), new ChunkPos(player.getOnPos())) == Result.DENY) {
				event.setCanceled(true);
				sendDenied("You can't teleport here!", player);
			}
		}
	}

	@SubscribeEvent
	public static void onNetherPortalUse(EntityTravelToDimensionEvent event) {
		if (event.getDimension() != Level.NETHER) return;
		if (event.getEntity() instanceof Player player) {
			if (ProtectionManager.checkPlayerAction(player, Permission.USE_NETHER_PORTALS, player.level(), new ChunkPos(player.getOnPos())) == Result.DENY) {
				event.setCanceled(true);
				sendDenied("You can't use portals here!", player);
			}
		}
	}

	private static void sendDenied(String message, Player player) {
		player.displayClientMessage(Component.literal(message).withStyle(ChatFormatting.RED), true);
	}
}
