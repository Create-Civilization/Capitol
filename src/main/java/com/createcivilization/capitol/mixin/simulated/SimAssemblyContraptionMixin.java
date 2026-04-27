package com.createcivilization.capitol.mixin.simulated;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import dev.simulated_team.simulated.util.assembly.SimAssemblyContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.openjdk.nashorn.internal.runtime.doubleconv.CachedPowers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimAssemblyContraption.class)
public class SimAssemblyContraptionMixin {

	@Shadow
	public BlockPos anchor;

	@Unique
	private BlockPos capitol$startPos;

	@Inject(method = "searchMovedStructure", remap = false, at = @At("HEAD"))
	private void capitol$captureStartPos(Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		capitol$startPos = pos;
	}

	@Inject(method = "movementAllowed", remap = false, at = @At("HEAD"), cancellable = true)
	private void capitol$preventCrossClaimAssembly(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BlockPos start = anchor != null ? anchor : capitol$startPos;
		if (start == null) return;
		if (ProtectionManager.checkContraptionAssemble(level, new ChunkPos(start), new ChunkPos(pos)) == ProtectionManager.Result.DENY) {
			cir.setReturnValue(false);
		}
	}

}
