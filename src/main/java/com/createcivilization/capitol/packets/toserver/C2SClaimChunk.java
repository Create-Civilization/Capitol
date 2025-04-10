package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.Capitol;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;


public record C2SClaimChunk (ChunkPos pos) implements CustomPacketPayload {

	public static final Type<C2SClaimChunk> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "claim_chunk"));

	public static final StreamCodec<FriendlyByteBuf, C2SClaimChunk> STREAM_CODEC =
		StreamCodec.composite(
			NeoForgeStreamCodecs.CHUNK_POS, C2SClaimChunk::pos,
			C2SClaimChunk::new
		);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}