package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.common.data.Team;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record S2CChunkData(Vector3f chunkCoords, Team team) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CChunkData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_chunk_data"));


	public static final StreamCodec<ByteBuf, S2CChunkData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VECTOR3F,
		S2CChunkData::chunkCoords,
		Team.STREAM_CODEC,
		S2CChunkData::team,
		S2CChunkData::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
