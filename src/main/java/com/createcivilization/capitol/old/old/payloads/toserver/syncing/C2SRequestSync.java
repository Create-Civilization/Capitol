package com.createcivilization.capitol.old.old.payloads.toserver.syncing;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.old.payloads.DirectionalPayload;
import com.createcivilization.capitol.old.old.payloads.toserver.ServerPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SRequestSync(int toIgnore) implements DirectionalPayload.Server {

	public static final Type<C2SRequestSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "request_sync"));

	public static final StreamCodec<FriendlyByteBuf, C2SRequestSync> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, C2SRequestSync::toIgnore,
		C2SRequestSync::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	public static void server(Object payload, IPayloadContext context) {
		ServerPacketHandler.syncDataWithPlayer((ServerPlayer) context.player());
	}
}