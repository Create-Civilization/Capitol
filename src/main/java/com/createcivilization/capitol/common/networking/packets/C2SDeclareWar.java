package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

// player requests to declare war on the team with the given name; the server
// resolves the target and validates (permission, not self, not already at war)
public record C2SDeclareWar(String receivingTeamName) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SDeclareWar> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_declare_war"));

	public static final StreamCodec<ByteBuf, C2SDeclareWar> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		C2SDeclareWar::receivingTeamName,
		C2SDeclareWar::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}