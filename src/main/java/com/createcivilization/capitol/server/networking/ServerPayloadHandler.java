package com.createcivilization.capitol.server.networking;

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
import com.createcivilization.capitol.common.networking.packets.C2SDamageWand;
import com.createcivilization.capitol.common.networking.packets.C2SInvitePlayer;
import com.createcivilization.capitol.common.item.SubClaimWand;
import com.createcivilization.capitol.server.invites.InviteHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

	public static void handleChunkRequest(final C2SChunkRequest request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		ChunkPos chunkPos = new ChunkPos(request.packedChunkPos());
		Team team = database.getChunkOwner(chunkPos, context.player().level());
		if (team == null) {
			S2CChunkRemove packet = new S2CChunkRemove(request.packedChunkPos());
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) context.player().level(), chunkPos, packet);
			return;
		}
		S2CChunkData packet = new S2CChunkData(request.packedChunkPos(), team);
		PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) context.player().level(), chunkPos, packet);
	}

	private static boolean isOutsideClaimRadius(Player player, ChunkPos targetChunk) {
		int claimRadius = CapitolConfig.CLAIM_RADIUS.get();
		if (claimRadius <= 0) return false;
		ChunkPos playerChunk = player.chunkPosition();
		return Math.abs(playerChunk.x - targetChunk.x) > claimRadius
			|| Math.abs(playerChunk.z - targetChunk.z) > claimRadius;
	}

	public static void handleClaimChunk(final C2SClaimChunk request, final IPayloadContext context) {
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
		boolean skippedAdjacent = false;
		long[] packed = request.packedChunkPositions();
		for (long packedPos : packed) {
			if (claimed >= remaining) break;

			ChunkPos chunkPos = new ChunkPos(packedPos);
			if (isOutsideClaimRadius(player, chunkPos)) continue;

			Team existingOwner = database.getChunkOwner(chunkPos, player.level());
			if (existingOwner != null) continue;

			if (!database.isChunkAdjacentToOwnClaim(team, chunkPos, player.level())) {
				skippedAdjacent = true;
				continue;
			}

			database.claimChunk(team, chunkPos, player.level());
			S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team);
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);
			claimed++;
		}

		player.displayClientMessage(Component.literal("Claimed " + claimed + " chunk(s)").withStyle(ChatFormatting.GREEN), false);
		if (claimed == 0 && skippedAdjacent) {
			player.displayClientMessage(Component.translatable("commands.capitol.claim.not_adjacent").withStyle(ChatFormatting.RED), false);
		}
	}

	public static void handleUnclaimChunk(final C2SUnclaimChunk request, final IPayloadContext context) {
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
		long[] packed = request.packedChunkPositions();
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

	public static void handleInvitePlayer(final C2SInvitePlayer request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.INVITE_MEMBERS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.translatable("commands.capitol.team.invite.no_permission").withStyle(ChatFormatting.RED), false);
			return;
		}

		ServerPlayer playerToInvite = player.getServer() == null ? null : player.getServer().getPlayerList().getPlayerByName(request.playerName());
		if (playerToInvite == null) {
			player.displayClientMessage(Component.translatable("commands.capitol.team.invite.invalid_player").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (database.getPlayerTeam(playerToInvite) != null) {
			player.displayClientMessage(Component.translatable("commands.capitol.team.invite.target_in_team").withStyle(ChatFormatting.RED), false);
			return;
		}

		InviteHandler.addInvite(playerToInvite, team);
		player.displayClientMessage(Component.translatable("commands.capitol.team.invite.success",
			Component.literal(playerToInvite.getName().getString()).withStyle(ChatFormatting.WHITE))
			.withStyle(ChatFormatting.GRAY), false);
	}

	public static void handleDamageWand(C2SDamageWand packet, IPayloadContext context) {
    context.enqueueWork(() -> {
        ServerPlayer player = (ServerPlayer) context.player();
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof SubClaimWand) {
            held.hurtAndBreak(1, (ServerLevel) player.level(),
                player, item -> {});
        }
    });
}
}
