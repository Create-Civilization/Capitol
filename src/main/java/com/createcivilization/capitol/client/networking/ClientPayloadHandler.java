package com.createcivilization.capitol.client.networking;

import com.createcivilization.capitol.client.events.ChunkEvents;
import com.createcivilization.capitol.client.journeymap.ClientJMClaims;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

	public static void chunkDataHandler(final S2CChunkData chunkData, final IPayloadContext context) {
		ChunkPos chunkPos = new ChunkPos(chunkData.packedChunkPos());
		Team team = chunkData.team();
		ClientClaimCache.addClaim(chunkPos, team);
		ClientJMClaims.instance().upsert(chunkPos, team);
		ClientJMClaims.instance().flush();
		ChunkEvents.onClaimInfoUpdated(chunkPos);
	}

	public static void chunkRemoveHandler(final S2CChunkRemove chunkData, final IPayloadContext context) {
		ChunkPos chunkPos = new ChunkPos(chunkData.packedChunkPos());
		ClientClaimCache.removeClaim(chunkPos);
		ClientJMClaims.instance().remove(chunkPos);
		ClientJMClaims.instance().flush();
		ChunkEvents.onClaimInfoUpdated(chunkPos);
	}
}
