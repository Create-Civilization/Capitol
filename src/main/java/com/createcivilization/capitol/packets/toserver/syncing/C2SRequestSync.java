package com.createcivilization.capitol.packets.toserver.syncing;

import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SRequestSync {

	public C2SRequestSync() {}

	public C2SRequestSync(FriendlyByteBuf friendlyByteBuf) {}

	public void encode(FriendlyByteBuf friendlyByteBuf) {}

	public void handle(NetworkEvent.Context context) {
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.syncDataWithPlayer(context.getSender()), context);
	}
}