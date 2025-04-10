package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.ClientPacketHandler;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

public record S2CRemoveChunk(String teamId, ChunkPos chunkPos, ResourceLocation dim) implements CustomPacketPayload {

	public static final Type<S2CRemoveChunk> TYPE = new Type<>(
		ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "remove_chunk")
	);

	public static final StreamCodec<FriendlyByteBuf, S2CRemoveChunk> STREAM_CODEC =
		StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, S2CRemoveChunk::teamId,
			NeoForgeStreamCodecs.CHUNK_POS, S2CRemoveChunk::chunkPos,
			ResourceLocation.STREAM_CODEC, S2CRemoveChunk::dim,
			S2CRemoveChunk::new
		);

	@NotNull
	@Override
	public Type<S2CRemoveChunk> type() {
		return TYPE;
	}
}

