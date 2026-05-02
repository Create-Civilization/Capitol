package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record C2SClaimChunk(Vector3f chunkCords) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SClaimChunk> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_claim_chunk"));

	public static final StreamCodec<ByteBuf, C2SClaimChunk> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VECTOR3F,
		C2SClaimChunk::chunkCords,
		C2SClaimChunk::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
