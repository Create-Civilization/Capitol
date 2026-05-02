package com.createcivilization.capitol.common.networking;

import com.createcivilization.capitol.client.networking.ClientPayloadHandler;
import com.createcivilization.capitol.common.networking.packets.C2SClaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SUnclaimChunk;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.C2SChunkRequest;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
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
		final PayloadRegistrar registrar = event.registrar("1");
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
	}

}
