package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record S2CChunkRemove(Vector3f chunkCords) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CChunkRemove> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_chunk_remove"));


	public static final StreamCodec<ByteBuf, S2CChunkRemove> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VECTOR3F,
		S2CChunkRemove::chunkCords,
		S2CChunkRemove::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
