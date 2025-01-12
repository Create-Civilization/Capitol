package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class C2SInvitePlayer {

	UUID playerToInvite;

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
