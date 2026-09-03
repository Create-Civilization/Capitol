package com.createcivilization.capitol.server.networking;

import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamMember;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.C2SBulkChunkRequest;
import com.createcivilization.capitol.common.networking.packets.C2SChunkAction;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.common.networking.packets.C2STeamChat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

	public static void handleBulkChunkRequest(final C2SBulkChunkRequest request, final IPayloadContext context) {
		for (long packedPos : request.packedChunkPositions()) {
			handleSingleChunk(packedPos, context);
		}
	}

	private static void handleSingleChunk(final long packedPos, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		ChunkPos chunkPos = new ChunkPos(packedPos);
		Team team = database.getChunkOwner(chunkPos, context.player().level());
		if (team == null) {
			S2CChunkRemove packet = new S2CChunkRemove(packedPos);
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) context.player().level(), chunkPos, packet);
			return;
		}
		S2CChunkData packet = new S2CChunkData(packedPos, team);
		PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) context.player().level(), chunkPos, packet);
	}

	private static boolean isOutsideClaimRadius(Player player, ChunkPos targetChunk) {
		int claimRadius = CapitolConfig.CLAIM_RADIUS.get();
		if (claimRadius <= 0) return false;
		ChunkPos playerChunk = player.chunkPosition();
		return Math.abs(playerChunk.x - targetChunk.x) > claimRadius
			|| Math.abs(playerChunk.z - targetChunk.z) > claimRadius;
	}

	public static void handleChunkAction(final C2SChunkAction request, final IPayloadContext context) {
		if (request.claim()) {
			handleClaimChunk(request.packedChunkPositions(), context);
		} else {
			handleUnclaimChunk(request.packedChunkPositions(), context);
		}
	}

	private static void handleClaimChunk(final long[] packed, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.literal("You are not in any team").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.literal("You do not have permission to claim chunks").withStyle(ChatFormatting.RED), false);
			return;
		}

		int serverLimit = CapitolConfig.MAX_TEAM_CLAIMS.get();
		int teamLimit = team.getMaxClaims();
		int effectiveLimit = (teamLimit > 0) ? Math.min(serverLimit, teamLimit) : serverLimit;
		int remaining = Math.max(0, effectiveLimit - team.getCurrentClaims());
		if (remaining <= 0) {
			boolean isServerLimit = (teamLimit <= 0) || (serverLimit <= teamLimit);
			String limitName = isServerLimit ? "server" : "team";
			int limitValue = isServerLimit ? serverLimit : teamLimit;
			player.displayClientMessage(Component.literal("Your team has reached the " + limitName + " claim limit (" + limitValue + ")").withStyle(ChatFormatting.RED), false);
			return;
		}

		int claimed = 0;
		for (long packedPos : packed) {
			if (claimed >= remaining) break;

			ChunkPos chunkPos = new ChunkPos(packedPos);
			if (isOutsideClaimRadius(player, chunkPos)) continue;

			Team existingOwner = database.getChunkOwner(chunkPos, player.level());
			if (existingOwner != null) continue;

			database.claimChunk(team, chunkPos, player.level());
			S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team);
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);
			claimed++;
		}

		player.displayClientMessage(Component.literal("Claimed " + claimed + " chunk(s)").withStyle(ChatFormatting.GREEN), false);
	}

	private static void handleUnclaimChunk(final long[] packed, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.literal("You are not in any team").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.UNCLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.literal("You do not have permission to unclaim chunks").withStyle(ChatFormatting.RED), false);
			return;
		}

		int unclaimed = 0;
		for (long packedPos : packed) {
			ChunkPos chunkPos = new ChunkPos(packedPos);
			if (isOutsideClaimRadius(player, chunkPos)) continue;

			Team existingOwner = database.getChunkOwner(chunkPos, player.level());
			if (existingOwner == null) continue;
			if (!existingOwner.getId().equals(team.getId())) continue;

			database.unclaimChunk(team, chunkPos, player.level());
			S2CChunkRemove packet = new S2CChunkRemove(chunkPos.toLong());
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);
			unclaimed++;
		}

		player.displayClientMessage(Component.literal("Unclaimed " + unclaimed + " chunk(s)").withStyle(ChatFormatting.GREEN), false);
	}

	public static void handleTeamChat(final C2STeamChat request, final IPayloadContext context) {
    CapitolDatabase database = DatabaseManager.database;
    Player player = context.player();

    Team team = database.getPlayerTeam(player);
    if (team == null) {
        player.displayClientMessage(
            Component.literal("You are not in a team").withStyle(ChatFormatting.RED),
            false
        );
        return;
    }

    // getRGB() returns ARGB; mask off the alpha channel so TextColor.fromRgb gets a plain 24-bit RGB value
    int rgb = team.getColor().getRGB() & 0xFFFFFF;

    Component message = Component.empty()
        .append(Component.literal("[" + team.getName() + "] ")
            .withStyle(s -> s.withColor(TextColor.fromRgb(rgb))))
        .append(Component.literal("<" + player.getName().getString() + "> ")
            .withStyle(ChatFormatting.WHITE))
        .append(Component.literal(request.message())
            .withStyle(ChatFormatting.WHITE));

    for (TeamMember member : database.getTeamMembers(team)) {
        ServerPlayer online = context.player().getServer()
            .getPlayerList()
            .getPlayer(member.playerUUID());
        if (online != null) {
            online.sendSystemMessage(message);
        }
    }
	}
}
