package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

public class S2CAddTeam {

	private final Team toAdd;

	public S2CAddTeam(Team team) {
		this.toAdd = team;
	}

	public S2CAddTeam(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		try {
			this.toAdd = TeamUtils.parseTeam(friendlyByteBuf.readUtf());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.toAdd.toString());
	}

	public void handle(NetworkEvent.Context context) {
		ClientPacketHandler.handlePacket(() -> ClientPacketHandler.addTeam(this.toAdd), context);
	}
}