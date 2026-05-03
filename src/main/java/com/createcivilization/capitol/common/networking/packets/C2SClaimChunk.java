package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SClaimChunk(long packedChunkPos) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SClaimChunk> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_claim_chunk"));

	public static final StreamCodec<ByteBuf, C2SClaimChunk> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_LONG,
		C2SClaimChunk::packedChunkPos,
		C2SClaimChunk::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
