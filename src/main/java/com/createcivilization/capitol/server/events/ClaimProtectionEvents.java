package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.managers.ProtectionManager.Result;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ClaimProtectionEvents {

	@SubscribeEvent
	public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
		Level level = event.getLevel();
		Entity source = event.getExplosion().getDirectSourceEntity();

		event.getAffectedBlocks().removeIf(blockPos ->
			ProtectionManager.checkExplosion(level, new ChunkPos(blockPos), source) == Result.DENY);

		event.getAffectedEntities().removeIf(entity ->
			ProtectionManager.checkExplosion(level, new ChunkPos(entity.blockPosition()), source) == Result.DENY);
	}

	@SubscribeEvent
	public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
		Level level = (Level) event.getLevel();
		BlockPos pos = event.getPos();
		var state = level.getBlockState(pos);

		if (state.getBlock() instanceof FireBlock || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
			if (ProtectionManager.checkFireSpread(level, new ChunkPos(pos)) == Result.DENY) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onPistonPre(PistonEvent.Pre event) {
		Level level = (Level) event.getLevel();
		BlockPos pistonPos = event.getPos();
		ChunkPos pistonChunk = new ChunkPos(pistonPos);
		ChunkPos targetChunk = new ChunkPos(pistonPos.relative(event.getDirection()));

		if (!pistonChunk.equals(targetChunk)) {
			if (ProtectionManager.checkPistonCrossBoundary(level, pistonChunk, targetChunk) == Result.DENY) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onFluidPlaceBlock(BlockEvent.FluidPlaceBlockEvent event) {
		Level level = (Level) event.getLevel();
		ChunkPos sourceChunk = new ChunkPos(event.getLiquidPos());
		ChunkPos targetChunk = new ChunkPos(event.getPos());

		if (!sourceChunk.equals(targetChunk)) {
			if (ProtectionManager.checkFluidFlow(level, sourceChunk, targetChunk) == Result.DENY) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onEntityMobGriefing(EntityMobGriefingEvent event) {
		Entity entity = event.getEntity();
		if (ProtectionManager.checkMobGriefing(entity, entity.level(), new ChunkPos(entity.blockPosition())) == Result.DENY) {
			event.setCanGrief(false);
		}
	}

	@SubscribeEvent
	public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
		if (event.getEntity() instanceof Player) return;

		if (ProtectionManager.checkCropTrample(event.getEntity().level(), new ChunkPos(event.getPos())) == Result.DENY) {
			event.setCanceled(true);
		}
	}
}
