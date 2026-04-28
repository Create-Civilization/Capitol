package com.createcivilization.capitol.server.commands.team;

import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

class TeamForceloadCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("forceload")
			.then(Commands.literal("chunk")
				.executes(TeamForceloadCommand::forceloadChunk));
	}

	private static int forceloadChunk(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		if (!CapitolConfig.FORCELOAD_ENABLED.get()) {
			context.getSource().sendFailure(Component.literal("Chunk forceloading is disabled on this server")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.FORCELOAD_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to forceload chunks")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		ChunkPos chunkPos = player.chunkPosition();
		Team owner = database.getChunkOwner(chunkPos, player.level());
		if (owner == null || !owner.getId().equals(team.getId())) {
			context.getSource().sendFailure(Component.literal("Your team does not own this chunk").withStyle(ChatFormatting.RED));
			return 0;
		}

		boolean isCurrentlyForced = database.isChunkForceloaded(chunkPos, player.level());
		if (!isCurrentlyForced) {
			int max = CapitolConfig.FORCELOAD_MAX_PER_TEAM.get();
			if (max > 0 && database.getTeamForceloadedCount(team) >= max) {
				context.getSource().sendFailure(Component.literal("Your team has reached the forceloaded chunk limit (" + max + ")")
					.withStyle(ChatFormatting.RED));
				return 0;
			}
		}

		boolean nowForced = database.toggleChunkForceload(team, chunkPos, player.level());
		ServerLevel level = context.getSource().getLevel();
		level.setChunkForced(chunkPos.x, chunkPos.z, nowForced);

		String state = nowForced ? "enabled" : "disabled";
		context.getSource().sendSuccess(() -> Component.literal("Forceloading " + state + " for this chunk.")
			.withStyle(ChatFormatting.GREEN), true);
		return 1;
	}
}