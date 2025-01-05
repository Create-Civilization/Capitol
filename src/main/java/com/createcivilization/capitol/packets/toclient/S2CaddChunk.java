package com.createcivilization.capitol.packets.toclient;

import com.createcivilization.capitol.packets.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CaddChunk {
	private final String claimingTeamId;
	private final ChunkPos chunkToAdd;
	private final ResourceLocation dimension;

	public S2CaddChunk(String teamId, ChunkPos chunkPos, ResourceLocation dim) {
		this.claimingTeamId = teamId;
		this.chunkToAdd = chunkPos;
		this.dimension = dim;
	}

	public S2CaddChunk(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.claimingTeamId = friendlyByteBuf.readUtf();
		this.chunkToAdd = friendlyByteBuf.readChunkPos();
		this.dimension = friendlyByteBuf.readResourceLocation();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.claimingTeamId);
		friendlyByteBuf.writeChunkPos(this.chunkToAdd);
		friendlyByteBuf.writeResourceLocation(this.dimension);
	}

	public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.addChunk(this.claimingTeamId, this.chunkToAdd, this.dimension))
		);

		ctx.setPacketHandled(true);
	}
}
