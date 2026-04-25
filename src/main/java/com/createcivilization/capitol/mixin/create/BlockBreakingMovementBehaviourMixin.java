package com.createcivilization.capitol.mixin.create;

import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.managers.ProtectionManager.Result;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents drills and ploughs from breaking blocks in foreign claims.
 * Uses the fake-bedrock approach: if the target is protected, we replace
 * the fetched block state with bedrock so Create skips it (hardness -1).
 * Approach inspired by OPAC (LGPL-3.0).
 */
@Mixin(value = BlockBreakingMovementBehaviour.class)
public class BlockBreakingMovementBehaviourMixin{

	@Unique
	private BlockPos capitol$capturedTargetPos;

	@ModifyArg(method = "tickBreaker", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockPos capitol$captureBreakPos(BlockPos pos) {
		capitol$capturedTargetPos = pos;
		return pos;
	}

	@ModifyVariable(method = "tickBreaker", ordinal = 0,
		at = @At(value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState capitol$preventBreakInForeignClaim(BlockState actual, MovementContext context) {
		if (capitol$capturedTargetPos == null || context.contraption.entity == null || context.world.isClientSide()) return actual;

		ChunkPos anchorChunk = new ChunkPos(context.contraption.anchor);
		ChunkPos targetChunk = new ChunkPos(capitol$capturedTargetPos);

		if (ProtectionManager.checkContraptionAction(context.world, anchorChunk, targetChunk) == Result.DENY) {
			return Blocks.BEDROCK.defaultBlockState();
		}
		return actual;
	}
}
