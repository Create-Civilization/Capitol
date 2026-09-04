package com.createcivilization.capitol.server.networking;

import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.SubClaim;
import com.createcivilization.capitol.common.data.SubClaimProtection;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamMember;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.C2SClaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SUnclaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SChunkRequest;
import com.createcivilization.capitol.common.networking.packets.C2SCreateSubClaim;
import com.createcivilization.capitol.common.networking.packets.C2SDamageWand;
import com.createcivilization.capitol.common.networking.packets.C2STeamChat;
import com.createcivilization.capitol.common.networking.packets.S2CSubClaimData;
import com.createcivilization.capitol.common.networking.packets.S2CSubClaimRemove;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.common.item.SubClaimWand;
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
import com.createcivilization.capitol.common.networking.packets.S2CSubClaimData;
import net.minecraft.core.BlockPos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
		long[] packed = request.packedChunkPositions();
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
		// de-duplicated across chunks: one sub-claim can touch several unclaimed chunks
		Map<UUID, SubClaim> removedSubClaims = new LinkedHashMap<>();
		long[] packed = request.packedChunkPositions();
		for (long packedPos : packed) {
			ChunkPos chunkPos = new ChunkPos(packedPos);
			if (isOutsideClaimRadius(player, chunkPos)) continue;

			Team existingOwner = database.getChunkOwner(chunkPos, player.level());
			if (existingOwner == null) continue;
			if (!existingOwner.getId().equals(team.getId())) continue;

			for (SubClaim subClaim : database.unclaimChunk(team, chunkPos, player.level())) {
				removedSubClaims.putIfAbsent(subClaim.id(), subClaim);
			}
			S2CChunkRemove packet = new S2CChunkRemove(chunkPos.toLong());
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);
			unclaimed++;
		}

		player.displayClientMessage(Component.literal("Unclaimed " + unclaimed + " chunk(s)").withStyle(ChatFormatting.GREEN), false);

		for (SubClaim subClaim : removedSubClaims.values()) {
			PacketDistributor.sendToAllPlayers(new S2CSubClaimRemove(subClaim.id()));
		}
		if (!removedSubClaims.isEmpty()) {
			player.displayClientMessage(Component.translatable("commands.capitol.sub_claim.removed_on_unclaim", removedSubClaims.size()).withStyle(ChatFormatting.RED), false);
		}
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

	public static void handleCreateSubClaim(final C2SCreateSubClaim packet, final IPayloadContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) context.player();
			CapitolDatabase database = DatabaseManager.database;

			Team team = database.getPlayerTeam(player);
			if (team == null) return;

			if (!Permission.CLAIM_SUB_CLAIMS.hasPermission(database.getPlayerPermission(player, team))) {
				player.displayClientMessage(Component.literal("You do not have permission to create sub-claims").withStyle(ChatFormatting.RED), false);
				return;
			}

			if (packet.name().isBlank()) return;

			// the client's naming screen caps input at 32 chars; enforce it server-side too
			if (packet.name().length() > 32) {
				player.displayClientMessage(Component.translatable("commands.capitol.sub_claim.name_too_long", 32).withStyle(ChatFormatting.RED), false);
				return;
			}

			// Normalise corners server-side so min is always <= max
			int minX = Math.min(packet.minX(), packet.maxX());
			int minY = Math.min(packet.minY(), packet.maxY());
			int minZ = Math.min(packet.minZ(), packet.maxZ());
			int maxX = Math.max(packet.minX(), packet.maxX());
			int maxY = Math.max(packet.minY(), packet.maxY());
			int maxZ = Math.max(packet.minZ(), packet.maxZ());

			String dimension = packet.dimension();
			if (!dimension.equals(player.level().dimension().location().toString())) {
				player.displayClientMessage(Component.translatable("commands.capitol.sub_claim.outside_claim").withStyle(ChatFormatting.RED), false);
				return;
			}

			// a player cannot have two sub-claims with the same name
			for (SubClaim owned : database.getSubClaimsOwnedBy(player.getUUID())) {
				if (owned.name().equalsIgnoreCase(packet.name())) {
					player.displayClientMessage(Component.translatable("commands.capitol.sub_claim.name_exists", packet.name()).withStyle(ChatFormatting.RED), false);
					return;
				}
			}

			// every chunk the sub-claim touches must be claimed by the player's team
			int minChunkX = Math.floorDiv(minX, 16);
			int maxChunkX = Math.floorDiv(maxX, 16);
			int minChunkZ = Math.floorDiv(minZ, 16);
			int maxChunkZ = Math.floorDiv(maxZ, 16);
			for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
				for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
					Team chunkOwner = database.getChunkOwner(new ChunkPos(chunkX, chunkZ), player.level());
					if (chunkOwner == null || !chunkOwner.getId().equals(team.getId())) {
						player.displayClientMessage(Component.translatable("commands.capitol.sub_claim.outside_claim").withStyle(ChatFormatting.RED), false);
						return;
					}
				}
			}

			// no sub-claim on the server may overlap this one
			for (SubClaim existing : database.getAllSubClaims()) {
				if (!existing.dimension().equals(dimension)) continue;
				if (existing.minX() <= maxX && existing.maxX() >= minX
					&& existing.minY() <= maxY && existing.maxY() >= minY
					&& existing.minZ() <= maxZ && existing.maxZ() >= minZ) {
					player.displayClientMessage(Component.translatable("commands.capitol.sub_claim.overlaps").withStyle(ChatFormatting.RED), false);
					return;
				}
			}

			SubClaim subClaim = new SubClaim(
				UUID.randomUUID(),
				team.getId(),
				packet.name(),
				dimension,
				minX, minY, minZ,
				maxX, maxY, maxZ,
				player.getUUID(),
				Permission.of(Permission.values()),
				SubClaimProtection.configDefaults()
			);

			database.addSubClaim(subClaim);

			// broadcast to players near the sub-claim
			int centreX = (subClaim.minX() + subClaim.maxX()) / 2;
			int centreZ = (subClaim.minZ() + subClaim.maxZ()) / 2;
			ChunkPos centreChunk = new ChunkPos(new BlockPos(centreX, 0, centreZ));
			S2CSubClaimData subClaimPacket = new S2CSubClaimData(
				subClaim.id(), subClaim.dimension(),
				subClaim.minX(), subClaim.minY(), subClaim.minZ(),
				subClaim.maxX(), subClaim.maxY(), subClaim.maxZ()
			);
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), centreChunk, subClaimPacket);

			player.displayClientMessage(Component.translatable("commands.capitol.sub_claim.created", packet.name()), false);
		});
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
