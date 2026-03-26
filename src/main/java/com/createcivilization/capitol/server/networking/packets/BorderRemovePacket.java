package com.createcivilization.capitol.server.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record BorderRemovePacket(Vector3f chunkCords) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<BorderRemovePacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "border_remove_data"));


	public static final StreamCodec<ByteBuf, BorderRemovePacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VECTOR3F,
		BorderRemovePacket::chunkCords,
		BorderRemovePacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
