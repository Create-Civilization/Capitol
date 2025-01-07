package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SClaimChunk {

	public C2SClaimChunk() {
	}

	public C2SClaimChunk(FriendlyByteBuf friendlyByteBuf) {
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
	}

	public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(
				Dist.DEDICATED_SERVER,
				() -> () -> ServerPacketHandler.claimCurrentPlayerChunk(ctx.getSender())
			)
		);
	}
}
