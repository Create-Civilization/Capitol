package com.createcivilization.capitol.client.networking;

import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.common.networking.packets.S2CClaimRadius;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

	public static void chunkDataHandler(final S2CChunkData chunkData, final IPayloadContext context) {
		ChunkPos chunkPos = new ChunkPos((int) chunkData.chunkCords().x, (int) chunkData.chunkCords().z);
		Team team = chunkData.team();
		ClientClaimCache.addClaim(chunkPos, team);
	}

	public static void chunkRemoveHandler(final S2CChunkRemove chunkData, final IPayloadContext context) {
		ChunkPos chunkPos = new ChunkPos((int) chunkData.chunkCords().x, (int) chunkData.chunkCords().z);
		ClientClaimCache.removeClaim(chunkPos);
	}

	public static void claimRadiusHandler(final S2CClaimRadius payload, final IPayloadContext context) {
		ClientClaimCache.setClaimRadius(payload.radius());
	}
}
