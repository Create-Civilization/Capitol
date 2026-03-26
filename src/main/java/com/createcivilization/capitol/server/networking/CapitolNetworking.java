package com.createcivilization.capitol.server.networking;

import com.createcivilization.capitol.client.networking.ClientPayloadHandler;
import com.createcivilization.capitol.server.networking.packets.BorderPacket;
import com.createcivilization.capitol.server.networking.packets.BorderRemovePacket;
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
			BorderPacket.TYPE,
			BorderPacket.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ClientPayloadHandler::handleBorderPacketOnMain
			)
		);

		registrar.playToClient(
			BorderRemovePacket.TYPE,
			BorderRemovePacket.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ClientPayloadHandler::handleBorderRemovePacketOnMain
			)
		);
	}

}
