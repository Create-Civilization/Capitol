package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.Capitol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SSendTeamMessage (String message) implements CustomPacketPayload {

	public static final Type<C2SSendTeamMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "send_team_message"));

	public static final StreamCodec<FriendlyByteBuf, C2SSendTeamMessage> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, C2SSendTeamMessage::message,
			C2SSendTeamMessage::new
		);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}