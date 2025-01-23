package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class C2SSendTeamMessage {

	private final String message;

	public C2SSendTeamMessage(String message) {
		this.message = message;
	}

	public C2SSendTeamMessage(FriendlyByteBuf friendlyByteBuf) {
		this.message = friendlyByteBuf.readUtf();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.message);
	}

	public void handle(NetworkEvent.Context context) {
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.sendTeamMessage(context.getSender(), this.message), context);
	}
}