package com.createcivilization.capitol.mixin.create;

import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.managers.ProtectionManager.Result;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents contraptions from assembling blocks from foreign claims
 * and from disassembling (placing) blocks into foreign claims.
 * Approach inspired by OPAC (LGPL-3.0).
 */
@Mixin(value = Contraption.class, priority = 1000001)
public abstract class ContraptionMixin {

	@Shadow
	public BlockPos anchor;

	@Unique
	private BlockPos capitol$capturedTargetPos;

	/** Prevents assembling blocks from a different team's claim. */
	@Inject(method = "movementAllowed", remap = false, at = @At("HEAD"), cancellable = true)
	private void capitol$preventCrossClaimAssembly(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (anchor == null) return;
		if (ProtectionManager.checkContraptionAssemble(level, new ChunkPos(anchor), new ChunkPos(pos)) == Result.DENY) {
			cir.setReturnValue(false);
		}
	}

	/**
	 * Captures the target position during disassembly so we can check it.
	 * Hooks the getBlockState call inside addBlocksToWorld's loop.
	 */
	@ModifyArg(method = "addBlocksToWorld", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockPos capitol$captureDisassemblyPos(BlockPos pos) {
		capitol$capturedTargetPos = pos;
		return pos;
	}

	/**
	 * Replaces the fetched block state with bedrock if the target position is in a foreign claim.
	 * Create sees bedrock (hardness -1) and drops the block as an item instead of placing it.
	 */
	@ModifyVariable(method = "addBlocksToWorld", ordinal = 1,
		at = @At(value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState capitol$preventDisassemblyInForeignClaim(BlockState actual, Level level, StructureTransform transform) {
		if (capitol$capturedTargetPos == null || anchor == null) return actual;
		if (ProtectionManager.checkContraptionAssemble(level, new ChunkPos(anchor), new ChunkPos(capitol$capturedTargetPos)) == Result.DENY) {
			return Blocks.BEDROCK.defaultBlockState();
		}
		return actual;
	}
}
