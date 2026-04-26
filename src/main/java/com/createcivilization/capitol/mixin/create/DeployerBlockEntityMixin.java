package com.createcivilization.capitol.mixin.create;

import com.createcivilization.capitol.common.compat.sable.SableCompat;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.managers.ProtectionManager.Result;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DeployerBlockEntity.class, priority = 1001)
public class DeployerBlockEntityMixin {

	@Shadow
	protected DeployerFakePlayer player;

	@Unique
	private boolean capitol$deny = false;

	@ModifyArg(
		method = "activate",
		remap = false,
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerHandler;activate(Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/Vec3;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity$Mode;)V"),
		index = 2
	)
	private BlockPos capitol$captureAndCheck(BlockPos clickedPos) {
		capitol$deny = false;
		if (this.player == null) return clickedPos;
		Level level = this.player.level();
		if (level.isClientSide()) return clickedPos;

		BlockPos deployerPos = ((BlockEntity)(Object)this).getBlockPos();
		Level rootLevel = player.serverLevel().getLevel();

		boolean deny;
		if (SableCompat.LOADED) {
			SubLevelAccess targetSubLevel = SableCompanion.INSTANCE.getContaining(rootLevel, clickedPos);
			if (targetSubLevel != null) {
				SubLevelAccess deployerSubLevel = SableCompanion.INSTANCE.getContaining(rootLevel, deployerPos);
				Result result = deployerSubLevel != null
					? ProtectionManager.checkSubLevelToSublevelActorAction(level, deployerSubLevel, targetSubLevel)
					: ProtectionManager.checkWorldToSubLevelActorAction(level, new ChunkPos(deployerPos), targetSubLevel);
				deny = result == Result.DENY;
			} else {
				SubLevelAccess deployerSubLevel = SableCompanion.INSTANCE.getContaining(rootLevel, deployerPos);
				if (deployerSubLevel != null) {
					deny = ProtectionManager.checkSubLevelToWorldActorAction(level, deployerSubLevel, new ChunkPos(clickedPos)) == Result.DENY;
				} else {
					deny = ProtectionManager.checkContraptionAction(level, new ChunkPos(deployerPos), new ChunkPos(clickedPos)) == Result.DENY;
				}
			}
		} else {
			deny = ProtectionManager.checkContraptionAction(level, new ChunkPos(deployerPos), new ChunkPos(clickedPos)) == Result.DENY;
		}

		capitol$deny = deny;
		return clickedPos;
	}

	@Inject(
		method = "activate",
		remap = false,
		at = @At(value = "INVOKE",
			target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerHandler;activate(Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/Vec3;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity$Mode;)V"),
		cancellable = true
	)
	private void capitol$cancelIfDenied(CallbackInfo ci) {
		if (capitol$deny) {
			capitol$deny = false;
			ci.cancel();
		}
	}
}