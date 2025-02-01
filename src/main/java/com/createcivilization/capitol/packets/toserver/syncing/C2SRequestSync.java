package com.createcivilization.capitol.packets.toserver.syncing;

import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

@SuppressWarnings("EmptyMethod")
public class C2SRequestSync {

	public C2SRequestSync() {}

	public C2SRequestSync(FriendlyByteBuf friendlyByteBuf) {}

	public void encode(FriendlyByteBuf friendlyByteBuf) {}

	public void handle(NetworkEvent.Context context) {
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.syncDataWithPlayer(context.getSender()), context);
	}
}