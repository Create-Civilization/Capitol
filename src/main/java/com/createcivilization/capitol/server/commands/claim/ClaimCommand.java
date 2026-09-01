package com.createcivilization.capitol.server.commands.claim;

import com.createcivilization.capitol.common.compat.sable.SableCompat;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.server.events.AutoClaimEvents;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class ClaimCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		LiteralArgumentBuilder<CommandSourceStack> infoBuilder = Commands.literal("info")
			.executes(ClaimCommand::info);

		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("claim")
			.then(Commands.literal("chunk")
				.executes(ClaimCommand::claim));

		builder.then(Commands.literal("auto")
			.executes(ClaimCommand::auto));

		if (SableCompat.LOADED) {
			builder.then(Commands.literal("sub_level")
				.executes(ClaimCommand::claimSubLevel));
			infoBuilder.then(Commands.literal("sub_level")
				.executes(ClaimCommand::infoSubLevel));
		}

		return builder.then(infoBuilder);
	}

	private static int claim(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.claim.no_permission").withStyle(ChatFormatting.RED));
			return 0;
		}

		int serverLimit = CapitolConfig.MAX_TEAM_CLAIMS.get();
		int teamLimit = team.getMaxClaims();
		int effectiveLimit = (teamLimit > 0) ? Math.min(serverLimit, teamLimit) : serverLimit;

		if (team.getCurrentClaims() >= effectiveLimit) {
			boolean isServerLimit = (teamLimit <= 0) || (serverLimit <= teamLimit);
			String limitName = isServerLimit ? "server" : "team";
			int limitValue = isServerLimit ? serverLimit : teamLimit;
			context.getSource().sendFailure(Component.translatable("commands.capitol.claim.limit_reached", limitName, limitValue).withStyle(ChatFormatting.RED));
			return 0;
		}

		ChunkPos chunkPos = player.chunkPosition();
		Team existingOwner = database.getChunkOwner(chunkPos, player.level());
		if (existingOwner != null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.claim.already_claimed", existingOwner.getName()).withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!database.isChunkAdjacentToOwnClaim(team, chunkPos, player.level())) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.claim.not_adjacent").withStyle(ChatFormatting.RED));
			return 0;
		}

		database.claimChunk(team, chunkPos, player.level());
		S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team);
		PacketDistributor.sendToPlayersTrackingChunk(context.getSource().getLevel(), chunkPos, packet);

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.claim.success").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int claimSubLevel(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.claim.no_permission").withStyle(ChatFormatting.RED));
			return 0;
		}

		UUID subLevelId = SableCompat.getPlayerSubLevelId(player);
		if (subLevelId == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.not_in_sub_level").withStyle(ChatFormatting.RED));
			return 0;
		}

		Team existingOwner = database.getSubLevelOwner(subLevelId);
		if (existingOwner != null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.claim.sub_level.already_claimed", existingOwner.getName()).withStyle(ChatFormatting.RED));
			return 0;
		}

		database.claimSubLevel(subLevelId, team);
		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.claim.sub_level.success").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int info(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team chunkOwner = database.getChunkOwner(player.chunkPosition(), player.level());
		if (chunkOwner == null) {
			context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.claim.info.unclaimed").withStyle(ChatFormatting.YELLOW), false);
			return 1;
		}

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.claim.info.owner",
			Component.literal(chunkOwner.getName()).withStyle(ChatFormatting.GOLD))
			.withStyle(ChatFormatting.GRAY), false);
		return 1;
	}

	private static int infoSubLevel(CommandContext<CommandSourceStack> context) {
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		UUID subLevelId = SableCompat.getPlayerSubLevelId(player);
		if (subLevelId == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.not_in_sub_level").withStyle(ChatFormatting.RED));
			return 0;
		}

		Team owner = DatabaseManager.database.getSubLevelOwner(subLevelId);
		if (owner == null) {
			context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.claim.sub_level.info.unclaimed").withStyle(ChatFormatting.YELLOW), false);
		} else {
			context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.claim.info.owner",
				Component.literal(owner.getName()).withStyle(ChatFormatting.GOLD))
				.withStyle(ChatFormatting.GRAY), false);
		}
		return 1;
	}

	private static int auto(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.claim.no_permission").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (AutoClaimEvents.isAutoClaimer(player)) {
			AutoClaimEvents.removeAutoClaimer(player);
			context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.claim.auto_claiming.stop").withStyle(ChatFormatting.GREEN), true);
		} else {
			AutoClaimEvents.updateAutoClaimer(player);
			context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.claim.auto_claiming.start").withStyle(ChatFormatting.GREEN), true);
		}
		return 1;
	}
}
