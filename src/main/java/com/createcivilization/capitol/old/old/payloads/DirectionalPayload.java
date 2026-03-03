package com.createcivilization.capitol.old.old.payloads;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface DirectionalPayload extends CustomPacketPayload {
	StreamCodec codec();

	interface Client extends DirectionalPayload {
		static void client(Object payload, IPayloadContext context) {

		}
	}

	interface Server extends DirectionalPayload {
		static void server(Object payload, IPayloadContext context) {

		}
	}
	interface BiDirectional extends DirectionalPayload.Client, DirectionalPayload.Server {}
}
