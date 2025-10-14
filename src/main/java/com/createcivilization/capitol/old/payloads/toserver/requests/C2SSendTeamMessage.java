package com.createcivilization.capitol.old.payloads.toserver.requests;

import com.createcivilization.capitol.Capitol;

import com.createcivilization.capitol.old.payloads.DirectionalPayload;
import com.createcivilization.capitol.old.payloads.toserver.ServerPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SSendTeamMessage (String message) implements DirectionalPayload.Server {

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

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	public static void server(Object payload, IPayloadContext context) {
		ServerPacketHandler.sendTeamMessage( (ServerPlayer) context.player(), ((C2SSendTeamMessage) payload).message());
	}
}