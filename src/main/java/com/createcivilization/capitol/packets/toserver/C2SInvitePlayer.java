package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class C2SInvitePlayer {

	private final UUID playerToInvite;

	public C2SInvitePlayer(UUID playerToInvite) {
		this.playerToInvite = playerToInvite;
	}

	public C2SInvitePlayer(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.playerToInvite = friendlyByteBuf.readUUID();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.playerToInvite);
	}

	public void handle(NetworkEvent.Context context) {
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.invitePlayerToTeam(context.getSender(), this.playerToInvite), context);
	}
}