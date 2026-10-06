package com.createcivilization.capitol.common.networking;

import com.createcivilization.capitol.client.networking.ClientPayloadHandler;
import com.createcivilization.capitol.common.networking.packets.C2SClaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SUnclaimChunk;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.C2SChunkRequest;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.common.networking.packets.C2SDamageWand;
import com.createcivilization.capitol.common.networking.packets.C2SInvitePlayer;
import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolScreen;
import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolNamingScreen;
import com.createcivilization.capitol.common.networking.packets.C2SNameCapitolBlock;
import com.createcivilization.capitol.common.networking.packets.C2SCancelCapitolBlockNaming;
import com.createcivilization.capitol.common.networking.packets.C2SSetCapitolBlockMayor;
import com.createcivilization.capitol.common.networking.packets.C2STeamChat;
import com.createcivilization.capitol.common.networking.packets.C2SUpgradeCapitolBlock;
import com.createcivilization.capitol.common.networking.packets.C2SDeclareWar;
import com.createcivilization.capitol.common.networking.packets.C2SEndWar;
import com.createcivilization.capitol.common.networking.packets.S2CSyncWars;
import com.createcivilization.capitol.server.networking.ServerPayloadHandler;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CapitolNetworking {

	public static void init(IEventBus bus) {
		bus.addListener(CapitolNetworking::register);
	}

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("3");

		registrar.playToClient(
			S2CChunkData.TYPE,
			S2CChunkData.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ClientPayloadHandler::chunkDataHandler
			)
		);

		registrar.playToClient(
			S2CChunkRemove.TYPE,
			S2CChunkRemove.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ClientPayloadHandler::chunkRemoveHandler
			)
		);

		registrar.playToClient(
			S2COpenCapitolScreen.TYPE,
			S2COpenCapitolScreen.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ClientPayloadHandler::openCapitolScreenHandler
			)
		);

		registrar.playToClient(
			S2COpenCapitolNamingScreen.TYPE,
			S2COpenCapitolNamingScreen.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ClientPayloadHandler::openCapitolNamingScreenHandler
			)
		);

		registrar.playToServer(
			C2SChunkRequest.TYPE,
			C2SChunkRequest.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleChunkRequest
			)
		);

		registrar.playToServer(
			C2SClaimChunk.TYPE,
			C2SClaimChunk.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleClaimChunk
			)
		);

		registrar.playToServer(
			C2SUnclaimChunk.TYPE,
			C2SUnclaimChunk.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleUnclaimChunk
			)
		);

		registrar.playToServer(
			C2SDamageWand.TYPE,
			C2SDamageWand.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleDamageWand
			)
		);

		registrar.playToServer(
			C2SInvitePlayer.TYPE,
			C2SInvitePlayer.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleInvitePlayer
			)
		);

		registrar.playToServer(
			C2STeamChat.TYPE,
			C2STeamChat.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleTeamChat
			)
		);

		registrar.playToServer(
			C2SUpgradeCapitolBlock.TYPE,
			C2SUpgradeCapitolBlock.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleUpgradeCapitolBlock
			)
		);

		registrar.playToServer(
			C2SNameCapitolBlock.TYPE,
			C2SNameCapitolBlock.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleNameCapitolBlock
			)
		);

		registrar.playToServer(
			C2SCancelCapitolBlockNaming.TYPE,
			C2SCancelCapitolBlockNaming.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleCancelCapitolBlockNaming
			)
		);

		registrar.playToServer(
			C2SSetCapitolBlockMayor.TYPE,
			C2SSetCapitolBlockMayor.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleSetCapitolBlockMayor
			)
		);

		registrar.playToClient(
			S2CSyncWars.TYPE,
			S2CSyncWars.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ClientPayloadHandler::syncWarsHandler
			)
		);

		registrar.playToServer(
			C2SDeclareWar.TYPE,
			C2SDeclareWar.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleDeclareWar
			)
		);

		registrar.playToServer(
			C2SEndWar.TYPE,
			C2SEndWar.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleEndWar
			)
		);
	}

}