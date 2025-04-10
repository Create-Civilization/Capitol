package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.Capitol;

import com.createcivilization.capitol.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public record C2SCreateTeam (String teamName, Color teamColor) implements CustomPacketPayload {

	public static final Type<C2SCreateTeam> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "create_team"));

	public static final StreamCodec<FriendlyByteBuf, C2SCreateTeam> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, C2SCreateTeam::teamName,
			PacketHandler.COLOR_CODEC, C2SCreateTeam::teamColor,
			C2SCreateTeam::new
		);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}