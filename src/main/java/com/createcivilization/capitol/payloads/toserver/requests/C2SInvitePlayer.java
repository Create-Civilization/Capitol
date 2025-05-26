package com.createcivilization.capitol.payloads.toserver.requests;

import com.createcivilization.capitol.Capitol;

import com.createcivilization.capitol.payloads.DirectionalPayload;
import com.createcivilization.capitol.payloads.toserver.ServerPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record C2SInvitePlayer (String playerToInvite) implements DirectionalPayload.Server {

	public static final Type<C2SInvitePlayer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "invite_player"));

	public static final StreamCodec<FriendlyByteBuf, C2SInvitePlayer> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, C2SInvitePlayer::playerToInvite,
			C2SInvitePlayer::new
		);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	public static void server(Object payload, IPayloadContext context) {
		ServerPacketHandler.invitePlayerToTeam( (ServerPlayer) context.player(), ((C2SInvitePlayer) payload).playerToInvite());
	}
}