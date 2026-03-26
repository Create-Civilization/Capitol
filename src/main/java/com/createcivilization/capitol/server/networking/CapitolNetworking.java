package com.createcivilization.capitol.server.networking;

import com.createcivilization.capitol.client.networking.ClientPayloadHandler;
import com.createcivilization.capitol.server.networking.packets.BoarderPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class CapitolNetworking {

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(
			BoarderPacket.TYPE,
			BoarderPacket.STREAM_CODEC,
			new MainThreadPayloadHandler<>(
				ClientPayloadHandler::handleDataOnMain
			)
		);
	}

}
