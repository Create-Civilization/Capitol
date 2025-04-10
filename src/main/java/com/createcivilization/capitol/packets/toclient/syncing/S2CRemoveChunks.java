package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.Capitol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record S2CRemoveChunks(String claimingTeamId, List<ChunkPos> chunksToAdd, ResourceLocation dimension) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CRemoveChunks> TYPE = new CustomPacketPayload.Type<>(
		ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "remove_chunks")
	);

	public static final StreamCodec<FriendlyByteBuf, S2CRemoveChunks> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public @NotNull S2CRemoveChunks decode(FriendlyByteBuf buf) {
			String claimingTeamId = buf.readUtf();
			int chunkCount = buf.readVarInt();
			List<ChunkPos> chunksToAdd = new ArrayList<>(chunkCount);
			for (int i = 0; i < chunkCount; i++) {
				chunksToAdd.add(NeoForgeStreamCodecs.CHUNK_POS.decode(buf));
			}
			ResourceLocation dimension = ResourceLocation.STREAM_CODEC.decode(buf);
			return new S2CRemoveChunks(claimingTeamId, chunksToAdd, dimension);
		}

		@Override
		public void encode(FriendlyByteBuf buf, S2CRemoveChunks payload) {
			buf.writeUtf(payload.claimingTeamId());
			buf.writeVarInt(payload.chunksToAdd().size());
			for (ChunkPos chunkPos : payload.chunksToAdd()) {
				NeoForgeStreamCodecs.CHUNK_POS.encode(buf, chunkPos);
			}
			ResourceLocation.STREAM_CODEC.encode(buf, payload.dimension());
		}
	};

	@NotNull
	@Override
	public CustomPacketPayload.Type<S2CRemoveChunks> type() {
		return TYPE;
	}
}