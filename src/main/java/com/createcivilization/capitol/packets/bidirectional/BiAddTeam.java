package com.createcivilization.capitol.packets.bidirectional;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.DirectionalPayload;
import com.createcivilization.capitol.packets.toclient.ClientPacketHandler;
import com.createcivilization.capitol.packets.toserver.ServerPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record BiAddTeam(Team team) implements DirectionalPayload.BiDirectional {

	public static final Type<BiAddChunk> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "bi_add_team"));

	public static final StreamCodec<FriendlyByteBuf, BiAddTeam> STREAM_CODEC =
		StreamCodec.composite(
			PacketHandler.TEAM_CODEC, BiAddTeam::team,
			BiAddTeam::new
		);

	public static void client(Object payload, IPayloadContext context) {
		ClientPacketHandler.addTeam(((BiAddTeam) payload).team());
	}

	public static void server(Object payload, IPayloadContext context) {
		ServerPacketHandler.createTeam(((BiAddTeam) payload).team().getName(), ((ServerPlayer) context.player()), ((BiAddTeam) payload).team().getColor());
	}

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
