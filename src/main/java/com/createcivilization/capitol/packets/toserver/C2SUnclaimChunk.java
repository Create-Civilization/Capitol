package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import net.minecraftforge.network.NetworkEvent;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

public class C2SUnclaimChunk {

	private final ChunkPos pos;

	public C2SUnclaimChunk(ChunkPos pos) {
		this.pos = pos;
	}

	public C2SUnclaimChunk(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readChunkPos();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		if (pos != null) friendlyByteBuf.writeChunkPos(this.pos);
	}

	public void handle(NetworkEvent.Context context) {
		ServerPlayer sender = context.getSender();
		if (sender == null) return;
		ObjectHolder<Team> holder = TeamUtils.getTeam(sender);
		if (holder.isEmpty()) return;
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.unclaimChunk(TeamUtils.getPlayerDimension(sender), this.pos, holder.getOrThrow()), context);
	}
}