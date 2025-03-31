package com.createcivilization.capitol.util;

import com.createcivilization.capitol.packets.toclient.gui.S2COpenTeamStatistics;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {

	public static final StreamCodec<FriendlyByteBuf, ChunkPos> CHUNK_POS_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.VAR_LONG, ChunkPos::toLong,
			ChunkPos::new
		);

	public static void register(PayloadRegistrar registrar) {
		registrar.commonToClient(
			S2COpenTeamStatistics.TYPE,
			S2COpenTeamStatistics.STREAM_CODEC,
			new DirectionalPayloadHandler<>(PacketHandler::stfu, PacketHandler::stfu)
		);
	}

	public static void stfu(Object payload, IPayloadContext context) {}

	public static void sendToServer(Object msg) {
		PacketDistributor.sendToServer((CustomPacketPayload) msg);
	}

	public static void sendToPlayer(Object msg, ServerPlayer player) {
		PacketDistributor.sendToPlayer(player, (CustomPacketPayload) msg);
	}

	public static void sendToAllPlayers(Object msg) {
		PacketDistributor.sendToAllPlayers((CustomPacketPayload) msg);
	}
}