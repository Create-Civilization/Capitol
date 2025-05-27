package com.createcivilization.capitol.payloads.bidirectional.add;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.payloads.DirectionalPayload;
import com.createcivilization.capitol.payloads.toclient.ClientPacketHandler;
import com.createcivilization.capitol.payloads.toserver.ServerPacketHandler;
import com.createcivilization.capitol.util.team.TeamUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BiAddChunk(ChunkPos chunkToAdd, @Nullable String claimingTeamID, @Nullable ResourceLocation dimension) implements DirectionalPayload.BiDirectional {

	public static final Type<BiAddChunk> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "bi_add_chunk"));

	public static final StreamCodec<FriendlyByteBuf, BiAddChunk> STREAM_CODEC =
		StreamCodec.composite(
			NeoForgeStreamCodecs.CHUNK_POS, BiAddChunk::chunkToAdd,
			ByteBufCodecs.STRING_UTF8, BiAddChunk::claimingTeamID,
			ResourceLocation.STREAM_CODEC, BiAddChunk::dimension,
			BiAddChunk::new
		);

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	public static void client(Object payload, IPayloadContext context) {
		ClientPacketHandler.addChunk(((BiAddChunk) payload).claimingTeamID(), ((BiAddChunk) payload).chunkToAdd(), TeamUtils.getPlayerDimension(context.player()));
	}

	public static void server(Object payload, IPayloadContext context) {
		Player player = context.player();
		ServerPacketHandler.claimChunk(TeamUtils.getPlayerDimension(player), ((BiAddChunk) payload).chunkToAdd, TeamUtils.getTeam(player).get());
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
