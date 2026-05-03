package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SUnclaimChunk(long packedChunkPos) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SUnclaimChunk> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_unclaim_chunk"));

	public static final StreamCodec<ByteBuf, C2SUnclaimChunk> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_LONG,
		C2SUnclaimChunk::packedChunkPos,
		C2SUnclaimChunk::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
