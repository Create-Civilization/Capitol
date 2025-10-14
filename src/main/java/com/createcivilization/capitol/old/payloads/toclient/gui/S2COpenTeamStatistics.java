package com.createcivilization.capitol.old.payloads.toclient.gui;

import com.createcivilization.capitol.Capitol;

import com.createcivilization.capitol.old.payloads.DirectionalPayload;
import com.createcivilization.capitol.old.payloads.toclient.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record S2COpenTeamStatistics(String teamID) implements DirectionalPayload.Client {

	public static final Type<S2COpenTeamStatistics> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "open_team_statistics")
	);

	public static final StreamCodec<FriendlyByteBuf, S2COpenTeamStatistics> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			S2COpenTeamStatistics::teamID,
			S2COpenTeamStatistics::new
		);

	@NotNull
	@Override
	public Type<S2COpenTeamStatistics> type() {
		return TYPE;
	}

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	public static void client(Object payload, IPayloadContext context) {
		ClientPacketHandler.openTeamStatistics(((S2COpenTeamStatistics) payload).teamID());
	}
}