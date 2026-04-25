package com.createcivilization.capitol.mixin.create;

import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
	private void capitol$stopBreakInClaim(BlockState stateToBreak, float blockHardness, CallbackInfoReturnable<Boolean> cir){
		if (this.level == null || this.level.isClientSide()) return;
		if(DatabaseManager.database.getChunkOwner(new ChunkPos(breakingPos), this.level) != null){
			cir.setReturnValue(false);
		}
	}

}
