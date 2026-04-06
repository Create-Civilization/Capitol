package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.common.data.*;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.awt.*;
import java.util.UUID;

public class TeamCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("team")
			.then(Commands.literal("create")
				.then(Commands.argument("name", StringArgumentType.string())
					.then(Commands.argument("tag", StringArgumentType.word())
						.then(Commands.argument("color", StringArgumentType.word())
							.then(Commands.argument("description", StringArgumentType.string())
								.executes(TeamCommand::createTeam))
							.executes(TeamCommand::createTeam)))))
			.then(Commands.literal("info").executes(TeamCommand::teamInfo))
			.then(TeamManageCommand.register());
	}

	private static int teamInfo(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		//TODO: Make the info stuff look nice. I am to lazy to do so.
		Component chatMsg = Component.literal("Put Info Here");

		context.getSource().sendSuccess(() -> chatMsg, true);
		return 1;
	}

	private static int createTeam(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		if (database.getPlayerTeam(player) != null) {
			context.getSource().sendFailure(Component.literal("You are already in a team, please leave before creating a new one")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String name = StringArgumentType.getString(context, "name");
		String hex = StringArgumentType.getString(context, "color").replace("#", "");
		String tag = StringArgumentType.getString(context, "tag");

		String description;
		try {
			description = StringArgumentType.getString(context, "description");
		} catch (IllegalArgumentException e) {
			description = "";
		}

		Color color;
		try {
			color = new Color((int) Long.parseLong(hex, 16), true);
		} catch (NumberFormatException e) {
			context.getSource().sendFailure(Component.literal("Invalid hex color: #" + hex).withStyle(ChatFormatting.RED));
			return 0;
		}

		Team team = Team.builder().name(name).id(UUID.randomUUID()).color(color).description(description).tag(tag).build();
		database.addTeam(team);
		TeamRole ownerRole = database.getRoleByName(team, TeamRole.OWNER_ROLE_NAME);
		database.addPlayerToTeam(player, team, ownerRole);
		context.getSource().sendSuccess(() -> Component.literal("Team Created!").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}
}