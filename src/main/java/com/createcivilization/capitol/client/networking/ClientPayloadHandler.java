package com.createcivilization.capitol.client.networking;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.server.networking.packets.BoarderPacket;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class ClientPayloadHandler {

	public static void handleDataOnMain(final BoarderPacket packet, final IPayloadContext context) {
		Capitol.LOGGER.info("RECEIVED{}", packet.temp());
	}
}
