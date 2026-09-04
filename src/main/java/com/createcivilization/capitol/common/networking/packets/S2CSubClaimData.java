package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.Capitol;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record S2CSubClaimData(
	UUID id,
	String dimension,
	int minX, int minY, int minZ,
	int maxX, int maxY, int maxZ
) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CSubClaimData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "s2c_sub_claim_data"));

	public static final StreamCodec<ByteBuf, S2CSubClaimData> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public S2CSubClaimData decode(ByteBuf buffer) {
			UUID id = UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(buffer));
			String dimension = ByteBufCodecs.STRING_UTF8.decode(buffer);
			int minX = ByteBufCodecs.INT.decode(buffer);
			int minY = ByteBufCodecs.INT.decode(buffer);
			int minZ = ByteBufCodecs.INT.decode(buffer);
			int maxX = ByteBufCodecs.INT.decode(buffer);
			int maxY = ByteBufCodecs.INT.decode(buffer);
			int maxZ = ByteBufCodecs.INT.decode(buffer);
			return new S2CSubClaimData(id, dimension, minX, minY, minZ, maxX, maxY, maxZ);
		}

		@Override
		public void encode(ByteBuf buffer, S2CSubClaimData value) {
			ByteBufCodecs.STRING_UTF8.encode(buffer, value.id().toString());
			ByteBufCodecs.STRING_UTF8.encode(buffer, value.dimension());
			ByteBufCodecs.INT.encode(buffer, value.minX());
			ByteBufCodecs.INT.encode(buffer, value.minY());
			ByteBufCodecs.INT.encode(buffer, value.minZ());
			ByteBufCodecs.INT.encode(buffer, value.maxX());
			ByteBufCodecs.INT.encode(buffer, value.maxY());
			ByteBufCodecs.INT.encode(buffer, value.maxZ());
		}
	};

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}