package com.createcivilization.capitol.mixin.create;

import com.createcivilization.capitol.common.compat.sable.SableCompat;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBreakingKineticBlockEntity.class)
public abstract class BlockBreakingKineticBlockEntityMixin extends BlockEntity {

	public BlockBreakingKineticBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	@Shadow
	protected BlockPos breakingPos;

	@Inject(method = "canBreak", remap = false, at = @At("HEAD"), cancellable = true)
	private void capitol$stopBreakInClaim(BlockState stateToBreak, float blockHardness, CallbackInfoReturnable<Boolean> cir) {
		if (this.level == null || this.level.isClientSide() || breakingPos == null) return;

		if (SableCompat.LOADED) {
			SubLevelAccess targetSubLevel = SableCompanion.INSTANCE.getContaining(this.level, breakingPos);
			if (targetSubLevel != null) {
				SubLevelAccess actorSubLevel = SableCompanion.INSTANCE.getContaining(this.level, this.worldPosition);
				if (ProtectionManager.checkSubLevelToSublevelActorAction(this.level, actorSubLevel, new ChunkPos(this.worldPosition), targetSubLevel) == ProtectionManager.Result.DENY) {
					cir.setReturnValue(false);
				}
				return;
			}

			SubLevelAccess actorSubLevel = SableCompanion.INSTANCE.getContaining(this.level, this.worldPosition);
			if (actorSubLevel != null) {
				if (ProtectionManager.checkSubLevelToWorldActorAction(this.level, actorSubLevel, new ChunkPos(breakingPos)) == ProtectionManager.Result.DENY) {
					cir.setReturnValue(false);
				}
				return;
			}
		}

		if (ProtectionManager.checkContraptionAction(this.level, new ChunkPos(this.worldPosition), new ChunkPos(breakingPos)) == ProtectionManager.Result.DENY) {
			cir.setReturnValue(false);
		}
	}
}
