package com.createcivilization.capitol.server.networking;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.C2SClaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SUnclaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SChunkRequest;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public class ServerPayloadHandler {

	public static void handleChunkRequest(final C2SChunkRequest request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		ChunkPos chunkPos = new ChunkPos((int) request.chunkCords().x, (int) request.chunkCords().z);
		Team team = database.getChunkOwner(chunkPos, context.player().level());
		if(team == null) return;
		S2CChunkData packet = new S2CChunkData(request.chunkCords(), team);
		PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) context.player().level(), chunkPos, packet);
	}

	public static void handleClaimChunk(final C2SClaimChunk request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();
		ChunkPos chunkPos = new ChunkPos((int) request.chunkCords().x, (int) request.chunkCords().z);

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.literal("You are not in any team").withStyle(ChatFormatting.RED), false);
			return;
		}

		ChunkPos playerChunk = player.chunkPosition();
		int claimRadius = CapitolConfig.CLAIM_RADIUS.get();
		if (claimRadius > 0 && (Math.abs(playerChunk.x - chunkPos.x) > claimRadius || Math.abs(playerChunk.z - chunkPos.z) > claimRadius)) {
			player.displayClientMessage(Component.literal("Chunk is too far away").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.literal("You do not have permission to claim chunks").withStyle(ChatFormatting.RED), false);
			return;
		}

		int serverLimit = CapitolConfig.MAX_TEAM_CLAIMS.get();
		int teamLimit = team.getMaxClaims();
		int effectiveLimit = (teamLimit > 0) ? Math.min(serverLimit, teamLimit) : serverLimit;

		if (team.getCurrentClaims() >= effectiveLimit) {
			boolean isServerLimit = (teamLimit <= 0) || (serverLimit <= teamLimit);
			String limitName = isServerLimit ? "server" : "team";
			int limitValue = isServerLimit ? serverLimit : teamLimit;
			player.displayClientMessage(Component.literal("Your team has reached the " + limitName + " claim limit (" + limitValue + ")").withStyle(ChatFormatting.RED), false);
			return;
		}

		Team existingOwner = database.getChunkOwner(chunkPos, player.level());
		if (existingOwner != null) {
			player.displayClientMessage(Component.literal("This chunk is already claimed by " + existingOwner.getName()).withStyle(ChatFormatting.RED), false);
			return;
		}

		database.claimChunk(team, chunkPos, player.level());
		S2CChunkData packet = new S2CChunkData(new Vector3f(chunkPos.x, 0, chunkPos.z), team);
		PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);

		player.displayClientMessage(Component.literal("Chunk claimed!").withStyle(ChatFormatting.GREEN), false);
	}

	public static void handleUnclaimChunk(final C2SUnclaimChunk request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();
		ChunkPos chunkPos = new ChunkPos((int) request.chunkCords().x, (int) request.chunkCords().z);

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.literal("You are not in any team").withStyle(ChatFormatting.RED), false);
			return;
		}

		ChunkPos playerChunk = player.chunkPosition();
		int claimRadius = CapitolConfig.CLAIM_RADIUS.get();
		if (claimRadius > 0 && (Math.abs(playerChunk.x - chunkPos.x) > claimRadius || Math.abs(playerChunk.z - chunkPos.z) > claimRadius)) {
			player.displayClientMessage(Component.literal("Chunk is too far away").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.UNCLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.literal("You do not have permission to unclaim chunks").withStyle(ChatFormatting.RED), false);
			return;
		}

		Team existingOwner = database.getChunkOwner(chunkPos, player.level());
		if (existingOwner == null) {
			player.displayClientMessage(Component.literal("This chunk is not claimed").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!existingOwner.getId().equals(team.getId())) {
			player.displayClientMessage(Component.literal("This chunk is not owned by your team").withStyle(ChatFormatting.RED), false);
			return;
		}

		database.unclaimChunk(team, chunkPos, player.level());
		S2CChunkRemove packet = new S2CChunkRemove(new Vector3f(chunkPos.x, 0, chunkPos.z));
		PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);

		player.displayClientMessage(Component.literal("Chunk unclaimed!").withStyle(ChatFormatting.GREEN), false);
	}
}
