package com.createcivilization.capitol.packets.toclient.gui;

import com.createcivilization.capitol.Capitol;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2COpenTeamStatistics(String teamID) implements CustomPacketPayload {

	public static final Type<S2COpenTeamStatistics> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "open_team_statistics")
	);

	public static final StreamCodec<FriendlyByteBuf, S2COpenTeamStatistics> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			S2COpenTeamStatistics::teamID,
			S2COpenTeamStatistics::new
		);

	@Override
	public Type<? extends CustomPacketPayload> type(){
		return TYPE;
	}

}