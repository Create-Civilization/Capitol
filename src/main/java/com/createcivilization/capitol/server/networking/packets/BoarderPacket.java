package com.createcivilization.capitol.server.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BoarderPacket(String temp) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<BoarderPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("mymod", "my_data"));


	public static final StreamCodec<ByteBuf, BoarderPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		BoarderPacket::temp,
		BoarderPacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
