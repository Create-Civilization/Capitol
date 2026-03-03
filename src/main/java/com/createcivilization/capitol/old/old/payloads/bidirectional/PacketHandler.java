package com.createcivilization.capitol.old.old.payloads.bidirectional;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.old.payloads.bidirectional.add.BiAddChunk;
import com.createcivilization.capitol.old.old.payloads.bidirectional.add.BiAddTeam;
import com.createcivilization.capitol.old.old.payloads.bidirectional.add.BiAddWar;
import com.createcivilization.capitol.old.old.payloads.bidirectional.remove.BiRemoveChunk;
import com.createcivilization.capitol.old.old.payloads.bidirectional.remove.BiRemoveWar;
import com.createcivilization.capitol.old.old.payloads.toclient.gui.S2COpenTeamStatistics;
import com.createcivilization.capitol.old.old.payloads.toclient.syncing.*;
import com.createcivilization.capitol.old.old.payloads.toserver.ServerPacketHandler;
import com.createcivilization.capitol.old.old.payloads.toserver.requests.*;
import com.createcivilization.capitol.old.old.payloads.toserver.syncing.C2SRequestSync;
import com.createcivilization.capitol.old.old.team.Team;
import com.createcivilization.capitol.old.old.team.War;
import com.createcivilization.capitol.old.old.util.data.GsonUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {

	public static final StreamCodec<FriendlyByteBuf, java.util.List<ChunkPos>> CHUNK_POS_LIST_CODEC =
		StreamCodec.of(
			(buf, chunkPosList) -> {
				buf.writeInt(chunkPosList.size());
				for (ChunkPos chunkPos : chunkPosList) {
					NeoForgeStreamCodecs.CHUNK_POS.encode(buf, chunkPos);
				}
			},
			(buf) -> {
				int size = buf.readInt();
				List<ChunkPos> chunkPosList = new ArrayList<>(size);
				for (int i = 0; i < size; i++) {
					ChunkPos chunkPos = NeoForgeStreamCodecs.CHUNK_POS.decode(buf);
					chunkPosList.add(chunkPos);
				}

				return chunkPosList;
			}
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

	public static final StreamCodec<FriendlyByteBuf, War> WAR_DATA_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8,
			GsonUtil::serializeWar,
			GsonUtil::deserializeWar
		);

	@SubscribeEvent
	private static void registerPackets(RegisterPayloadHandlersEvent event) {
		PacketHandler.register(event.registrar("1"));
	}

	public static void register(PayloadRegistrar registrar) {
		PentaConsumer<TriConsumer<CustomPacketPayload.Type, StreamCodec, IPayloadHandler>, CustomPacketPayload.Type, StreamCodec, BiConsumer<Object,IPayloadContext>, BiConsumer<Object,IPayloadContext>> directionalRegister = (direction, type, codec, client, server) -> direction.accept(
			type,
			codec,
			new DirectionalPayloadHandler<>(
				client::accept,
				server::accept
			)
		);

		QuadConsumer<CustomPacketPayload.Type, StreamCodec, BiConsumer<Object,IPayloadContext>, BiConsumer<Object,IPayloadContext>> registerBiDirectional = (type, codec, client, server) -> directionalRegister.accept(
			registrar::playBidirectional,
			type,
			codec,
			client,
			server
		);

		TriConsumer<CustomPacketPayload.Type, StreamCodec, BiConsumer<Object,IPayloadContext>> registerToClient = (type, codec, client) -> directionalRegister.accept(
			registrar::playToClient,
			type,
			codec,
			client,
			PacketHandler::empty
		);

		TriConsumer<CustomPacketPayload.Type, StreamCodec, BiConsumer<Object,IPayloadContext>> registerToServer = (type, codec, server) -> directionalRegister.accept(
			registrar::playToServer,
			type,
			codec,
			PacketHandler::empty,
			server
		);

		registerBiDirectional.accept(BiAddChunk.TYPE, BiAddChunk.STREAM_CODEC, BiAddChunk::client, BiAddChunk::server);
		registerBiDirectional.accept(BiAddTeam.TYPE, BiAddTeam.STREAM_CODEC, BiAddTeam::client, BiAddTeam::server);
		registerBiDirectional.accept(BiRemoveChunk.TYPE, BiRemoveChunk.STREAM_CODEC, BiRemoveChunk::client, BiRemoveChunk::server);
		registerBiDirectional.accept(BiAddWar.TYPE, BiAddWar.STREAM_CODEC, BiAddWar::client, BiAddWar::server);
		registerBiDirectional.accept(BiRemoveWar.TYPE, BiRemoveWar.STREAM_CODEC, BiRemoveWar::client, BiRemoveWar::server);

		registerToClient.accept(S2COpenTeamStatistics.TYPE, S2COpenTeamStatistics.STREAM_CODEC, S2COpenTeamStatistics::client);
		registerToClient.accept(S2CRemoveCapitol.TYPE, S2CRemoveCapitol.STREAM_CODEC, S2CRemoveCapitol::client);
		registerToClient.accept(S2CRemoveTeam.TYPE, S2CRemoveTeam.STREAM_CODEC, S2CRemoveTeam::client);

		registerToServer.accept(C2SClaimCurrentChunk.TYPE, C2SClaimCurrentChunk.STREAM_CODEC, C2SClaimCurrentChunk::server);
		registerToServer.accept(C2SInvitePlayer.TYPE, C2SInvitePlayer.STREAM_CODEC, C2SInvitePlayer::server);
		registerToServer.accept(C2SSendTeamMessage.TYPE, C2SSendTeamMessage.STREAM_CODEC, C2SSendTeamMessage::server);
		registerToServer.accept(C2SRequestSync.TYPE, C2SRequestSync.STREAM_CODEC, C2SRequestSync::server);
	}

	@FunctionalInterface
	public interface QuadConsumer<A, B, C, D> {
		void accept(A a, B b, C c, D d);
	}

	@FunctionalInterface
	public interface PentaConsumer<A, B, C, D, E> {
		void accept(A a, B b, C c, D d, E e);
	}

	public static void empty(Object payload, IPayloadContext context) {}

	public static void sendToServer(Object msg) {
		try {
			PacketDistributor.sendToServer((CustomPacketPayload) msg);
		} catch (RuntimeException e) {
			Capitol.LOGGER.error("Packet error, requesting re-sync", e);
			PacketDistributor.sendToServer(new C2SRequestSync(0));
		}
	}

	public static void sendToPlayer(Object msg, ServerPlayer player) {
		try {
			PacketDistributor.sendToPlayer(player, (CustomPacketPayload) msg);
		} catch (RuntimeException e) {
			Capitol.LOGGER.error("Packet error {}, requesting to re-sync player \"{}\"", e, player.getName().getString());
			ServerPacketHandler.syncDataWithPlayer(player);
		}
	}

	public static void sendToAllPlayers(Object msg) {
		PacketDistributor.sendToAllPlayers((CustomPacketPayload) msg);
	}
}