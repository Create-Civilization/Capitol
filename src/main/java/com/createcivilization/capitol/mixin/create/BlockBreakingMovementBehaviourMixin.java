package com.createcivilization.capitol.mixin.create;

import com.createcivilization.capitol.common.compat.sable.SableCompat;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.managers.ProtectionManager.Result;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = BlockBreakingMovementBehaviour.class, priority = 1001)
public class BlockBreakingMovementBehaviourMixin {

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

		BlockPos anchorPos = context.contraption.anchor;
		BlockPos targetPos = capitol$capturedTargetPos;

		if (SableCompat.LOADED) {
			SubLevelAccess targetSubLevel = SableCompanion.INSTANCE.getContaining(context.world, targetPos);
			if (targetSubLevel != null) {
				SubLevelAccess anchorSubLevel = SableCompanion.INSTANCE.getContaining(context.world, anchorPos);
				Result result = anchorSubLevel != null
					? ProtectionManager.checkSubLevelToSublevelActorAction(context.world, anchorSubLevel, targetSubLevel)
					: ProtectionManager.checkWorldToSubLevelActorAction(context.world, new ChunkPos(anchorPos), targetSubLevel);
				if (result == Result.DENY) return Blocks.BEDROCK.defaultBlockState();
				return actual;
			}
		}

		if (ProtectionManager.checkContraptionAction(context.world, new ChunkPos(anchorPos), new ChunkPos(targetPos)) == Result.DENY)
			return Blocks.BEDROCK.defaultBlockState();
		return actual;
	}
}