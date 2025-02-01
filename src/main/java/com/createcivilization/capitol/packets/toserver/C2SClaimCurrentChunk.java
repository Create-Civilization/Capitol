package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

@SuppressWarnings("EmptyMethod")
public class C2SClaimCurrentChunk {

	public C2SClaimCurrentChunk() {}

	public C2SClaimCurrentChunk(FriendlyByteBuf friendlyByteBuf){}

	public void encode(FriendlyByteBuf friendlyByteBuf) {}

	public void handle(NetworkEvent.Context context) {
		ServerPlayer sender = context.getSender();
		if (sender == null) return;
		ObjectHolder<Team> holder = TeamUtils.getTeam(sender);
		if (holder.isEmpty()) return;
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.claimChunk(TeamUtils.getPlayerDimension(sender), sender.chunkPosition(), holder.getOrThrow()), context);
	}
}
