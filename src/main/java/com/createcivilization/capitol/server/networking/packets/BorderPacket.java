package com.createcivilization.capitol.server.networking.packets;

import com.createcivilization.capitol.common.data.Team;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public record BorderPacket(Vector3f chunkCords, Team team) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<BorderPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "border_data"));


	public static final StreamCodec<ByteBuf, BorderPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VECTOR3F,
		BorderPacket::chunkCords,
		Team.STREAM_CODEC,
		BorderPacket::team,
		BorderPacket::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
