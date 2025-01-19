package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import net.minecraftforge.network.NetworkEvent;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;


public class C2SClaimChunk {

	private final ChunkPos pos;

	public C2SClaimChunk(ChunkPos pos) {
		this.pos = pos;
	}

	public C2SClaimChunk(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readChunkPos();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		if (pos != null) friendlyByteBuf.writeChunkPos(this.pos);
	}

	public void handle(NetworkEvent.Context context) {
		ServerPlayer sender = context.getSender();
		assert sender != null;
		ObjectHolder<Team> holder = TeamUtils.getTeam(sender);
		if (holder.isEmpty()) return;
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.claimChunk(sender.level().dimension().location(), this.pos, holder.getOrThrow()), context);
	}
}