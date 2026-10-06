package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

// player requests to end the war their team declared against the receiving team
public record C2SEndWar(UUID declaringTeamId, UUID receivingTeamId) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SEndWar> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_end_war"));

	public static final StreamCodec<ByteBuf, C2SEndWar> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
		C2SEndWar::declaringTeamId,
		ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString),
		C2SEndWar::receivingTeamId,
		C2SEndWar::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}