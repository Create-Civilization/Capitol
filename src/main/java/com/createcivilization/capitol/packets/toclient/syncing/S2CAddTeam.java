package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;

public class S2CAddTeam {

	private final Team toAdd;

	public S2CAddTeam(Team team) {
		this.toAdd = team;
	}

	public S2CAddTeam(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.toAdd = TeamUtils.parseTeam(friendlyByteBuf.readUtf());
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.toAdd.toString());
	}

	public void handle(NetworkEvent.Context context) {
		ClientPacketHandler.handlePacket(() -> ClientPacketHandler.addTeam(this.toAdd), context);
	}
}