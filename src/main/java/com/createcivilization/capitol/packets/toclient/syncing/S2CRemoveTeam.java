package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.Capitol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record S2CRemoveTeam(String toRemoveId) implements CustomPacketPayload {

	public static final Type<S2CRemoveTeam> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "remove_team"));

	public static final StreamCodec<FriendlyByteBuf, S2CRemoveTeam> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, S2CRemoveTeam::toRemoveId,
			S2CRemoveTeam::new
		);

	@NotNull
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}