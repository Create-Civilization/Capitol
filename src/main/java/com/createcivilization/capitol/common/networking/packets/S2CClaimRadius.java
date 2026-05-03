package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2CClaimRadius(int radius) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CClaimRadius> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_claim_radius"));

	public static final StreamCodec<ByteBuf, S2CClaimRadius> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT,
		S2CClaimRadius::radius,
		S2CClaimRadius::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
