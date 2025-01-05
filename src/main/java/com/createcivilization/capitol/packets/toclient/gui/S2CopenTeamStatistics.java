package com.createcivilization.capitol.packets.toclient.gui;

import com.createcivilization.capitol.packets.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CopenTeamStatistics {
	private final String teamId;

	public S2CopenTeamStatistics(String teamId) {
		this.teamId = teamId;
	}

	public S2CopenTeamStatistics(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.teamId = friendlyByteBuf.readUtf();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.teamId);
	}

	public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.openTeamStatistics(this.teamId))
		);

		ctx.setPacketHandled(true);
	}
}
