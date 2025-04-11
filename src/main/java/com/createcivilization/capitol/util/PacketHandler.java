package com.createcivilization.capitol.util;

import com.createcivilization.capitol.packets.ClientPacketHandler;
import com.createcivilization.capitol.packets.ServerPacketHandler;
import com.createcivilization.capitol.packets.toclient.gui.S2COpenTeamStatistics;
import com.createcivilization.capitol.packets.toclient.syncing.*;
import com.createcivilization.capitol.packets.toserver.*;
import com.createcivilization.capitol.packets.toserver.syncing.C2SRequestSync;
import com.createcivilization.capitol.team.Team;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.awt.*;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {

	public static final StreamCodec<FriendlyByteBuf, Color> COLOR_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.INT, Color::getRGB,
			Color::new
		);

	public static final StreamCodec<FriendlyByteBuf, Team> TEAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			GsonUtil::serialize,
			GsonUtil::deserializeTeam
		);

	public static final StreamCodec<FriendlyByteBuf, Team.CapitolData> CAPITOL_DATA_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			GsonUtil::serializeCapitol,
			GsonUtil::deserializeCapitol
		);

	@SubscribeEvent
	private static void registerPackets(RegisterPayloadHandlersEvent event) {
		PacketHandler.register(event.registrar("1"));
	}

	public static void register(PayloadRegistrar registrar) {
		registrar.playToClient(
			S2COpenTeamStatistics.TYPE,
			S2COpenTeamStatistics.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				(pack, context) -> ClientPacketHandler.openTeamStatistics(pack.teamID()),
				PacketHandler::empty
			)
		);
		registrar.playToClient(
			S2CAddChunk.TYPE,
			S2CAddChunk.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				(pack, context) -> ClientPacketHandler.addChunk(pack.claimingTeamID(), pack.chunkToAdd(), pack.dimension()),
				PacketHandler::empty
			)
		);
		registrar.playToClient(
			S2CAddTeam.TYPE,
			S2CAddTeam.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				(pack, context) -> ClientPacketHandler.addTeam(pack.team()),
				PacketHandler::empty
			)
		);
		registrar.playToClient(
			S2CRemoveCapitol.TYPE,
			S2CRemoveCapitol.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				(pack, context) -> ClientPacketHandler.removeCapitol(pack.capitolData(), pack.dimension(), pack.teamID()),
				PacketHandler::empty
			)
		);
		registrar.playToClient(
			S2CRemoveChunk.TYPE,
			S2CRemoveChunk.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				(pack, context) -> ClientPacketHandler.removeChunks(pack.teamId(), pack.chunkPos(), pack.dim()),
				PacketHandler::empty
			)
		);
		registrar.playToClient(
			S2CRemoveChunks.TYPE,
			S2CRemoveChunks.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				(pack, context) -> ClientPacketHandler.removeChunks(pack.claimingTeamId(), pack.chunksToAdd(), pack.dimension()),
				PacketHandler::empty
			)
		);
		registrar.playToClient(
			S2CRemoveTeam.TYPE,
			S2CRemoveTeam.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				(pack, context) -> ClientPacketHandler.removeTeam(pack.toRemoveId()),
				PacketHandler::empty
			)
		);

		registrar.playToServer(
			C2SRequestSync.TYPE,
			C2SRequestSync.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				PacketHandler::empty,
				(pack, context) -> ServerPacketHandler.syncDataWithPlayer((ServerPlayer) context.player())
			)
		);

		registrar.playToServer(
			C2SClaimChunk.TYPE,
			C2SClaimChunk.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				PacketHandler::empty,
				(pack, context) -> {
					ServerPlayer player = (ServerPlayer) context.player();
					ServerPacketHandler.claimChunk(TeamUtils.getPlayerDimension(player), pack.pos(), TeamUtils.getTeam(player).get());
				}
			)
		);

		registrar.playToServer(
			C2SClaimCurrentChunk.TYPE,
			C2SClaimCurrentChunk.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				PacketHandler::empty,
				(pack, context) -> {
					ServerPlayer player = (ServerPlayer) context.player();
					ServerPacketHandler.claimChunk(TeamUtils.getPlayerDimension(player), player.chunkPosition(), TeamUtils.getTeam(player).get());
				}
			)
		);
		registrar.playToServer(
			C2SCreateTeam.TYPE,
			C2SCreateTeam.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				PacketHandler::empty,
				(pack, context) -> ServerPacketHandler.createTeam(pack.teamName(), (ServerPlayer) context.player(), pack.teamColor())
			)
		);
		registrar.playToServer(
			C2SInvitePlayer.TYPE,
			C2SInvitePlayer.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				PacketHandler::empty,
				(pack, context) -> ServerPacketHandler.invitePlayerToTeam( (ServerPlayer) context.player(), pack.playerToInvite())
			)
		);
		registrar.playToServer(
			C2SSendTeamMessage.TYPE,
			C2SSendTeamMessage.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				PacketHandler::empty,
				(pack, context) -> ServerPacketHandler.sendTeamMessage( (ServerPlayer) context.player(), pack.message())
			)
		);
		registrar.playToServer(
			C2SUnclaimChunk.TYPE,
			C2SUnclaimChunk.STREAM_CODEC,
			new DirectionalPayloadHandler<>(
				PacketHandler::empty,
				(pack, context) -> {
					Player player = context.player();
					ServerPacketHandler.unclaimChunk(TeamUtils.getPlayerDimension(player), pack.pos(), TeamUtils.getTeam(player).get());
				}
			)
		);
	}

	public static void empty(Object payload, IPayloadContext context) {}

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