package com.createcivilization.capitol.client.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.networking.packets.C2SChunkRequest;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.CLIENT)
public class ChunkEvents {

	/**
	 * Handles sending packets to server when loading chunks to fetch claims and adding to cache
	 */
	@SubscribeEvent
	private static void onChunkLoad(ChunkEvent.Load event) {
		if (!event.getLevel().isClientSide()) return;
		if(ClientClaimCache.hasClaim(event.getChunk().getPos())) return;
		ChunkPos chunkPos = event.getChunk().getPos();
		C2SChunkRequest packet = new C2SChunkRequest(chunkPos.toLong());
		PacketDistributor.sendToServer(packet);
	}

	/**
	 * Removes chunks from client claim cache when they are unloaded
	 */
	@SubscribeEvent
	private static void onChunkUnload(ChunkEvent.Unload event) {
		if (!event.getLevel().isClientSide()) return;
		if(!ClientClaimCache.hasClaim(event.getChunk().getPos())) return;
		ClientClaimCache.removeClaim(event.getChunk().getPos());
	}

	/**
	 * Clears Cache On Client Disconnect
	 */
	@SubscribeEvent
	private static void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientClaimCache.clearClaims();
	}

}
