package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.Capitol;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SCreateSubClaim(
	String name,
	String dimension,
	int minX, int minY, int minZ,
	int maxX, int maxY, int maxZ
) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SCreateSubClaim> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "c2s_create_sub_claim"));

	public static final StreamCodec<ByteBuf, C2SCreateSubClaim> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public C2SCreateSubClaim decode(ByteBuf buffer) {
			String name = ByteBufCodecs.STRING_UTF8.decode(buffer);
			String dimension = ByteBufCodecs.STRING_UTF8.decode(buffer);
			int minX = ByteBufCodecs.INT.decode(buffer);
			int minY = ByteBufCodecs.INT.decode(buffer);
			int minZ = ByteBufCodecs.INT.decode(buffer);
			int maxX = ByteBufCodecs.INT.decode(buffer);
			int maxY = ByteBufCodecs.INT.decode(buffer);
			int maxZ = ByteBufCodecs.INT.decode(buffer);
			return new C2SCreateSubClaim(name, dimension, minX, minY, minZ, maxX, maxY, maxZ);
		}

		@Override
		public void encode(ByteBuf buffer, C2SCreateSubClaim value) {
			ByteBufCodecs.STRING_UTF8.encode(buffer, value.name());
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