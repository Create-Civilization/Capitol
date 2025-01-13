package com.createcivilization.capitol.packets.toclient.gui;

import com.createcivilization.capitol.packets.ClientPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

public class S2COpenTeamStatistics {

	private final String teamId;

	public S2COpenTeamStatistics(String teamId) {
		this.teamId = teamId;
	}

	public S2COpenTeamStatistics(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.teamId = friendlyByteBuf.readUtf();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.teamId);
	}

	public void handle(NetworkEvent.Context context) {
		ClientPacketHandler.handlePacket(() -> ClientPacketHandler.openTeamStatistics(this.teamId), context);
	}
}