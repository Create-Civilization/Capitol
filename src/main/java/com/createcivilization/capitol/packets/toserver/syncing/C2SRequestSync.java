package com.createcivilization.capitol.packets.toserver.syncing;

import com.createcivilization.capitol.Capitol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SRequestSync(int toIgnore) implements CustomPacketPayload {

	public static final Type<C2SRequestSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "request_sync"));

	public static final StreamCodec<FriendlyByteBuf, C2SRequestSync> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, C2SRequestSync::toIgnore,
		C2SRequestSync::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}