package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CRemoveTeam {

	private final String toRemoveId;

	public S2CRemoveTeam(String teamId) {
		this.toRemoveId = teamId;
	}

	public S2CRemoveTeam(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.toRemoveId = friendlyByteBuf.readUtf();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.toRemoveId);
	}

	public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.removeTeam(this.toRemoveId))
		);

		ctx.setPacketHandled(true);
	}
}