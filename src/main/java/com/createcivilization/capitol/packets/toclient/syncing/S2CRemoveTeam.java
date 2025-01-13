package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

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

	public void handle(NetworkEvent.Context context) {
		ClientPacketHandler.handlePacket(() -> ClientPacketHandler.removeTeam(this.toRemoveId), context);
	}
}