package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.common.data.Team;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record S2CChunkData(long packedChunkPos, Team team, Optional<Long> capitolBlockId) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CChunkData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_chunk_data"));


	public static final StreamCodec<ByteBuf, S2CChunkData> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_LONG,
		S2CChunkData::packedChunkPos,
		Team.STREAM_CODEC,
		S2CChunkData::team,
		ByteBufCodecs.optional(ByteBufCodecs.VAR_LONG),
		S2CChunkData::capitolBlockId,
		S2CChunkData::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}