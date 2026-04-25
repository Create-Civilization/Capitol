package com.createcivilization.capitol.mixin.create;

import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.managers.ProtectionManager.Result;
import com.simibubi.create.content.contraptions.actors.plough.PloughMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents ploughs from hoeing farmland in foreign claims.
 * The block-breaking part is already handled by {@link BlockBreakingMovementBehaviourMixin};
 * this covers the {@code useOn} (hoeing) call.
 * Approach inspired by OPAC (LGPL-3.0).
 */
@Mixin(PloughMovementBehaviour.class)
public class PloughMovementBehaviourMixin {

	@Inject(method = "visitNewPosition", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"),
		cancellable = true)
	private void capitol$preventPloughInForeignClaim(MovementContext context, BlockPos pos, CallbackInfo ci) {
		if (context.contraption.entity == null || context.world.isClientSide()) return;

		ChunkPos anchorChunk = new ChunkPos(context.contraption.anchor);
		ChunkPos targetChunk = new ChunkPos(pos);

		if (ProtectionManager.checkContraptionAction(context.world, anchorChunk, targetChunk) == Result.DENY) {
			ci.cancel();
		}
	}
}
