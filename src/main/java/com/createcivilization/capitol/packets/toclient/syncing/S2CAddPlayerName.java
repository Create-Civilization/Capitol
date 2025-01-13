package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class S2CAddPlayerName {

	private final String playerName;
	private final UUID playerUUID;

	public S2CAddPlayerName(ServerPlayer player) {
		this.playerName = player.getName().getString();
		this.playerUUID = player.getUUID();
	}

	public S2CAddPlayerName(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.playerName = friendlyByteBuf.readUtf();
		this.playerUUID = friendlyByteBuf.readUUID();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.playerName);
		friendlyByteBuf.writeUUID(this.playerUUID);
	}

	public void handle(NetworkEvent.Context context) {
		ClientPacketHandler.handlePacket(() -> ClientPacketHandler.addPlayerInfo(this.playerName, this.playerUUID), context);
	}
}