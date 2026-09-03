package com.createcivilization.capitol.common.networking;

import com.createcivilization.capitol.client.networking.ClientPayloadHandler;
import com.createcivilization.capitol.common.networking.packets.C2SBulkChunkRequest;
import com.createcivilization.capitol.common.networking.packets.C2SChunkAction;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.common.networking.packets.C2STeamChat;
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
		final PayloadRegistrar registrar = event.registrar("2");
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
			C2SBulkChunkRequest.TYPE,
			C2SBulkChunkRequest.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleBulkChunkRequest
			)
		);

		registrar.playToServer(
			C2SChunkAction.TYPE,
			C2SChunkAction.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ServerPayloadHandler::handleChunkAction
			)
		);

		registrar.playToServer(
    		C2STeamChat.TYPE,
    		C2STeamChat.STREAM_CODEC,
    		new MainThreadPayloadHandler<>(
    			ServerPayloadHandler::handleTeamChat
    		)
    	);
	}

}
