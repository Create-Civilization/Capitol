package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SInvitePlayer (String playerToInvite) implements CustomPacketPayload {

	public static final Type<C2SInvitePlayer> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "invite_player"));

	public static final StreamCodec<FriendlyByteBuf, C2SInvitePlayer> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, C2SInvitePlayer::playerToInvite,
			C2SInvitePlayer::new
		);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}