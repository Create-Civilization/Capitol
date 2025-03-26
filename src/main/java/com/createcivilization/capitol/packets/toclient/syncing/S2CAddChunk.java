package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.Capitol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;



public record S2CAddChunk(String claimingTeamID, ChunkPos chunkToAdd, ResourceLocation dimension) implements CustomPacketPayload {

	public static final Type<S2CAddChunk> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "add_chunk")
	);

	private static final StreamCodec<FriendlyByteBuf, ChunkPos> CHUNK_POS_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.INT, pos -> pos.x,
			ByteBufCodecs.INT, pos -> pos.z,
			ChunkPos::new
		);

	public static final StreamCodec<FriendlyByteBuf, S2CAddChunk> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, S2CAddChunk::claimingTeamID,
			CHUNK_POS_CODEC, S2CAddChunk::chunkToAdd,
			ResourceLocation.STREAM_CODEC, S2CAddChunk::dimension,
			S2CAddChunk::new
		);



	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

