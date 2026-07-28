package com.createcivilization.capitol.client.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.networking.packets.C2SBulkChunkRequest;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.CLIENT)
public class ChunkEvents {
	private static final int RGB_24_BIT_MASK = 0xFFFFFF;
	private static final Set<Long> pendingChunkRequests = ConcurrentHashMap.newKeySet();
	private static ChunkPos lastPlayerChunk = null;
	private static ChunkPos pendingChunkAnnouncement = null;
	private static boolean hasLastTerritory = false;
	private static UUID lastTerritoryTeamId = null;

	/**
	 * Handles sending packets to server when loading chunks to fetch claims and adding to cache
	 */
	@SubscribeEvent
	private static void onChunkLoad(ChunkEvent.Load event) {
		if (!event.getLevel().isClientSide()) return;
		if(ClientClaimCache.hasClaim(event.getChunk().getPos())) return;
		ChunkPos chunkPos = event.getChunk().getPos();
		pendingChunkRequests.add(chunkPos.toLong());
	}

	@SubscribeEvent
	private static void onClientTick(ClientTickEvent.Post event) {
		flushPendingChunkRequests();

		var player = Minecraft.getInstance().player;
		if (player == null) return;

		ChunkPos current = player.chunkPosition();
		if (current.equals(lastPlayerChunk)) return;
		lastPlayerChunk = current;

		Team team = ClientClaimCache.getClaim(current);
		if (team != null) {
			onTerritoryResolved(team);
			pendingChunkAnnouncement = null;
			return;
		}

		pendingChunkAnnouncement = current;
		PacketDistributor.sendToServer(new C2SBulkChunkRequest(new long[]{current.toLong()}));
	}

	private static void flushPendingChunkRequests() {
		if (pendingChunkRequests.isEmpty()) return;
		long[] packedChunkPositions = pendingChunkRequests.stream().mapToLong(Long::longValue).toArray();
		pendingChunkRequests.clear();
		if (packedChunkPositions.length == 0) return;
		PacketDistributor.sendToServer(new C2SBulkChunkRequest(packedChunkPositions));
	}

	public static void onClaimInfoUpdated(ChunkPos chunkPos) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		if (pendingChunkAnnouncement == null || !pendingChunkAnnouncement.equals(chunkPos)) return;
		if (!chunkPos.equals(player.chunkPosition())) return;

		Team team = ClientClaimCache.getClaim(chunkPos);
		onTerritoryResolved(team);
		pendingChunkAnnouncement = null;
	}

	private static void onTerritoryResolved(Team team) {
		UUID teamId = (team == null) ? null : team.getId();
		boolean changed = !hasLastTerritory || !Objects.equals(lastTerritoryTeamId, teamId);
		lastTerritoryTeamId = teamId;
		hasLastTerritory = true;

		if (!changed) return;

		if (team == null) {
			showWilderness();
		} else {
			showTerritory(team);
		}
	}

	private static void showTerritory(Team team) {
		var player = Minecraft.getInstance().player;
		if (player == null) return;
		int rgb = team.getColor().getRGB() & RGB_24_BIT_MASK;
		player.displayClientMessage(
			Component.literal(team.getName()).withStyle(style -> style.withColor(TextColor.fromRgb(rgb))),
			true
		);
	}

	private static void showWilderness() {
		var player = Minecraft.getInstance().player;
		if (player == null) return;
		player.displayClientMessage(Component.translatable("hud.capitol.territory.wilderness").withStyle(ChatFormatting.DARK_GREEN), true);
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
		pendingChunkRequests.clear();
		lastPlayerChunk = null;
		pendingChunkAnnouncement = null;
		hasLastTerritory = false;
		lastTerritoryTeamId = null;
	}

}
