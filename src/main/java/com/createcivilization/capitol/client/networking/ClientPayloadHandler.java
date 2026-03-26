package com.createcivilization.capitol.client.networking;

import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.server.networking.packets.BorderPacket;
import com.createcivilization.capitol.server.networking.packets.BorderRemovePacket;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

	public static void handleBorderPacketOnMain(final BorderPacket packet, final IPayloadContext context) {
		Team team = packet.team();
		ChunkPos chunkPos = new ChunkPos((int) packet.chunkCords().x, (int) packet.chunkCords().z);
		System.out.println("Adding Claim");
		ClientClaimCache.addClaim(chunkPos, team);
	}

	public static void handleBorderRemovePacketOnMain(final BorderRemovePacket packet, final IPayloadContext context) {
		ChunkPos chunkPos = new ChunkPos((int) packet.chunkCords().x, (int) packet.chunkCords().z);
		ClientClaimCache.removeClaim(chunkPos);
	}
}
