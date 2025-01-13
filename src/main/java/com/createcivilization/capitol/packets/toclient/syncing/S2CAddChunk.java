package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import net.minecraftforge.network.NetworkEvent;

public class S2CAddChunk {

	private final String claimingTeamId;
	private final ChunkPos chunkToAdd;
	private final ResourceLocation dimension;

	public S2CAddChunk(String teamId, ChunkPos chunkPos, ResourceLocation dim) {
		this.claimingTeamId = teamId;
		this.chunkToAdd = chunkPos;
		this.dimension = dim;
	}

	public S2CAddChunk(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.claimingTeamId = friendlyByteBuf.readUtf();
		this.chunkToAdd = friendlyByteBuf.readChunkPos();
		this.dimension = friendlyByteBuf.readResourceLocation();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.claimingTeamId);
		friendlyByteBuf.writeChunkPos(this.chunkToAdd);
		friendlyByteBuf.writeResourceLocation(this.dimension);
	}

	public void handle(NetworkEvent.Context context) {
		ClientPacketHandler.handlePacket(() -> ClientPacketHandler.addChunk(this.claimingTeamId, this.chunkToAdd, this.dimension), context);
	}
}