package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record S2CAddTeam(Team team) implements CustomPacketPayload {

	public static final Type<S2CAddTeam> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "add_team")
	);

	public static final StreamCodec<FriendlyByteBuf, S2CAddTeam> STREAM_CODEC =
		StreamCodec.composite(
			PacketHandler.TEAM_CODEC, S2CAddTeam::team,
			S2CAddTeam::new
		);

	@NotNull
	@Override
	public Type<S2CAddTeam> type() {
		return TYPE;
	}
}