package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CChunkRemove(long packedChunkPos) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CChunkRemove> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_chunk_remove"));


	public static final StreamCodec<ByteBuf, S2CChunkRemove> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_LONG,
		S2CChunkRemove::packedChunkPos,
		S2CChunkRemove::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
