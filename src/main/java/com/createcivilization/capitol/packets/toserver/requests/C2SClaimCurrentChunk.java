package com.createcivilization.capitol.packets.toserver.requests;

import com.createcivilization.capitol.Capitol;

import com.createcivilization.capitol.packets.DirectionalPayload;
import com.createcivilization.capitol.packets.toserver.ServerPacketHandler;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SClaimCurrentChunk(int toIgnore) implements DirectionalPayload.Server {
	//TODO:: REMOVE
	public static final CustomPacketPayload.Type<C2SClaimCurrentChunk> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "claim_current_chunk"));

	public static final StreamCodec<FriendlyByteBuf, C2SClaimCurrentChunk> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, C2SClaimCurrentChunk::toIgnore,
		C2SClaimCurrentChunk::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	public static void server(Object payload, IPayloadContext context) {
		ServerPlayer player = (ServerPlayer) context.player();
		ServerPacketHandler.claimChunk(TeamUtils.getPlayerDimension(player), player.chunkPosition(), TeamUtils.getTeam(player).get());
	}
}
