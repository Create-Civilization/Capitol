package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

public class C2SInvitePlayer {

	private final String playerToInvite;

	public C2SInvitePlayer(String playerToInvite) {
		this.playerToInvite = playerToInvite;
	}

	public C2SInvitePlayer(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.playerToInvite = friendlyByteBuf.readUtf();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.playerToInvite);
	}

	public void handle(NetworkEvent.Context context) {
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.invitePlayerToTeam(context.getSender(), this.playerToInvite), context);
	}
}