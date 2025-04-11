package com.createcivilization.capitol.packets.bidirectional;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.DirectionalPayload;
import com.createcivilization.capitol.packets.toclient.ClientPacketHandler;
import com.createcivilization.capitol.util.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BiRemoveChunk(ResourceLocation dimension, List<ChunkPos> chunks) implements DirectionalPayload.BiDirectional {

	public static final Type<BiAddChunk> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "bi_rem_chunk"));

	public static final StreamCodec<FriendlyByteBuf, BiRemoveChunk> STREAM_CODEC =
		StreamCodec.composite(
			ResourceLocation.STREAM_CODEC, BiRemoveChunk::dimension,
			PacketHandler.CHUNK_POS_LIST_CODEC, BiRemoveChunk::chunks,
			BiRemoveChunk::new
		);

	public static void client(Object payload, IPayloadContext context) {
		ClientPacketHandler.removeChunks(((BiRemoveChunk) payload).dimension, ((BiRemoveChunk) payload).chunks);
	}

	public static void server(Object payload, IPayloadContext context) {
		// TODO:: Unclaim packet
	}

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
