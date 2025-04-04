package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.ClientPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.GsonUtil;

import com.createcivilization.capitol.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
public record S2CRemoveCapitol(Team.CapitolData capitolData, ResourceLocation dimension, String teamID) implements CustomPacketPayload {

	public S2CRemoveCapitol(Team.CapitolData capitolData, ResourceLocation dimension, Team team) {
		this(capitolData, dimension, team.getTeamId());
	}

	public static final Type<S2CRemoveCapitol> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "remove_capitol")
	);

	public static final StreamCodec<FriendlyByteBuf, S2CRemoveCapitol> STREAM_CODEC =
		StreamCodec.composite(
			PacketHandler.CAPITOL_DATA_CODEC, S2CRemoveCapitol::capitolData,
			ResourceLocation.STREAM_CODEC, S2CRemoveCapitol::dimension,
			ByteBufCodecs.STRING_UTF8, S2CRemoveCapitol::teamID,
			S2CRemoveCapitol::new
		);

	@NotNull
	@Override
	public Type<S2CRemoveCapitol> type() {
		return TYPE;
	}
}