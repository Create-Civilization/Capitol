package com.createcivilization.capitol.client.networking;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.server.networking.packets.BorderPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

	public static void handleDataOnMain(final BorderPacket packet, final IPayloadContext context) {
		Capitol.LOGGER.info("RECEIVED{}", packet.temp());
	}
}
