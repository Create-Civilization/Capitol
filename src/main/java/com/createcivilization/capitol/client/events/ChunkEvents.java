package com.createcivilization.capitol.client.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.networking.packets.C2SChunkRequest;
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

import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.CLIENT)
public class ChunkEvents {
	private static ChunkPos lastPlayerChunk;
	private static ChunkPos pendingChunkAnnouncement;
	private static boolean hasLastTerritory;
	private static UUID lastTerritoryTeamId;

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

	@SubscribeEvent
	private static void onClientTick(ClientTickEvent.Post event) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
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
		PacketDistributor.sendToServer(new C2SChunkRequest(current.toLong()));
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
		int rgb = team.getColor().getRGB() & 0xFFFFFF;
		player.displayClientMessage(
			Component.literal(team.getName()).withStyle(style -> style.withColor(TextColor.fromRgb(rgb))),
			true
		);
	}

	private static void showWilderness() {
		var player = Minecraft.getInstance().player;
		if (player == null) return;
		player.displayClientMessage(Component.literal("Wilderness").withStyle(ChatFormatting.DARK_GREEN), true);
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
		lastPlayerChunk = null;
		pendingChunkAnnouncement = null;
		hasLastTerritory = false;
		lastTerritoryTeamId = null;
	}

}
