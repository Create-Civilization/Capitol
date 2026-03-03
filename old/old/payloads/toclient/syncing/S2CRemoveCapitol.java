package com.createcivilization.capitol.old.old.payloads.toclient.syncing;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.old.payloads.DirectionalPayload;
import com.createcivilization.capitol.old.old.payloads.toclient.ClientPacketHandler;
import com.createcivilization.capitol.old.old.team.OldTeam;

import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
public record S2CRemoveCapitol(OldTeam.CapitolData capitolData, ResourceLocation dimension, String teamID) implements DirectionalPayload.Client {

	public S2CRemoveCapitol(OldTeam.CapitolData capitolData, ResourceLocation dimension, OldTeam oldTeam) {
		this(capitolData, dimension, oldTeam.getTeamId());
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

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	public static void client(Object payload, IPayloadContext context) {
		ClientPacketHandler.removeCapitol(((S2CRemoveCapitol) payload).capitolData(), ((S2CRemoveCapitol) payload).dimension(), ((S2CRemoveCapitol) payload).teamID());
	}
}