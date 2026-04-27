package com.createcivilization.capitol.mixin.simulated;

import com.createcivilization.capitol.common.managers.ProtectionManager;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.simulated_team.simulated.content.blocks.physics_assembler.PhysicsAssemblerBlockEntity;
import dev.simulated_team.simulated.network.packets.UpdatePlayerUsingHandlePacket;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = UpdatePlayerUsingHandlePacket.class)
public class UpdatePlayerUsingHandlePacketMixin {

	@Shadow private BlockPos interactionPos;
	@Shadow private boolean remove;

	@Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
	public void capitol$blockPacket(ServerPacketContext context, CallbackInfo ci) {
		if (remove) return;

		Player player = context.player();
		Level level = context.level();
		BlockEntity blockEntity = level.getBlockEntity(interactionPos);
		Block block = blockEntity.getBlockState().getBlock();

		if (!(blockEntity instanceof PhysicsAssemblerBlockEntity)) {
			return;
		}

		SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, interactionPos);

		if(subLevel != null){
			if(ProtectionManager.checkBlockInteract(player, block, subLevel) == ProtectionManager.Result.DENY){
				ci.cancel();
			}
			return;
		}

		if(ProtectionManager.checkBlockInteract(player, block, level, new ChunkPos(interactionPos)) == ProtectionManager.Result.DENY){
			ci.cancel();
		}

	}
}