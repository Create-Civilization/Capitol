package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.Capitol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SClaimCurrentChunk(int toIgnore) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SClaimCurrentChunk> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "claim_current_chunk"));

	public static final StreamCodec<FriendlyByteBuf, C2SClaimCurrentChunk> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, C2SClaimCurrentChunk::toIgnore,
		C2SClaimCurrentChunk::new
	);


	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
