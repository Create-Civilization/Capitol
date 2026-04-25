package com.createcivilization.capitol.mixin.create;

import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.managers.ProtectionManager.Result;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Prevents harvesters from harvesting crops in foreign claims.
 * Replaces the fetched block state with bedrock (air-like skip) so
 * Create's harvester sees nothing to harvest.
 * Approach inspired by OPAC (LGPL-3.0).
 */
@Mixin(value = HarvesterMovementBehaviour.class)
public class HarvesterMovementBehaviourMixin {

	@ModifyVariable(method = "visitNewPosition",
		at = @At(value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState capitol$preventHarvestInForeignClaim(BlockState actual, MovementContext context, BlockPos pos) {
		if (context.contraption.entity == null) return actual;

		ChunkPos anchorChunk = new ChunkPos(context.contraption.anchor);
		ChunkPos targetChunk = new ChunkPos(pos);

		if (ProtectionManager.checkContraptionAction(context.world, anchorChunk, targetChunk) == Result.DENY) {
			return Blocks.AIR.defaultBlockState();
		}
		return actual;
	}
}
