package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class S2CRemoveChunks {

	private final String claimingTeamId;
	private final List<ChunkPos> chunksToAdd = new ArrayList<>();
	private final ResourceLocation dimension;

	public S2CRemoveChunks(String teamId, List<ChunkPos> chunkPos, ResourceLocation dim) {
		this.claimingTeamId = teamId;
		this.chunksToAdd.addAll(chunkPos);
		this.dimension = dim;
	}

	public S2CRemoveChunks(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.claimingTeamId = friendlyByteBuf.readUtf();
		int limit = friendlyByteBuf.readInt();
		for (int i = 0; i < limit; i++) {
			this.chunksToAdd.add(friendlyByteBuf.readChunkPos());
		}
		this.dimension = friendlyByteBuf.readResourceLocation();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.claimingTeamId);
		friendlyByteBuf.writeInt(this.chunksToAdd.size());
		this.chunksToAdd.forEach(friendlyByteBuf::writeChunkPos);
		friendlyByteBuf.writeResourceLocation(this.dimension);
	}

	public void handle(NetworkEvent.Context context) {
		ClientPacketHandler.handlePacket(() -> ClientPacketHandler.removeChunks(this.claimingTeamId, this.chunksToAdd, this.dimension), context);
	}
}