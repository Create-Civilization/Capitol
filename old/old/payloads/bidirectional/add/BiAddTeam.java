package com.createcivilization.capitol.old.old.payloads.bidirectional.add;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.old.payloads.DirectionalPayload;
import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.old.payloads.toclient.ClientPacketHandler;
import com.createcivilization.capitol.old.old.payloads.toserver.ServerPacketHandler;
import com.createcivilization.capitol.old.old.team.OldTeam;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record BiAddTeam(OldTeam oldTeam) implements DirectionalPayload.BiDirectional {

	public static final Type<BiAddChunk> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "bi_add_team"));

	public static final StreamCodec<FriendlyByteBuf, BiAddTeam> STREAM_CODEC =
		StreamCodec.composite(
			PacketHandler.TEAM_CODEC, BiAddTeam::oldTeam,
			BiAddTeam::new
		);

	public static void client(Object payload, IPayloadContext context) {
		ClientPacketHandler.addTeam(((BiAddTeam) payload).oldTeam());
	}

	public static void server(Object payload, IPayloadContext context) {
		ServerPacketHandler.createTeam(((BiAddTeam) payload).oldTeam().getName(), ((ServerPlayer) context.player()), ((BiAddTeam) payload).oldTeam().getColor());
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
