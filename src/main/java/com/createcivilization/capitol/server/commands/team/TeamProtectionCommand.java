package com.createcivilization.capitol.server.commands.team;

import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamProtection;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

class TeamProtectionCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("protection")
			.then(Commands.argument("protection_value", StringArgumentType.word())
				.suggests((context, builder) -> {
					for (TeamProtection tp : TeamProtection.values()) {
						if (ProtectionManager.isTeamConfigurable(tp)) {
							builder.suggest(tp.getKey());
						}
					}
					return builder.buildFuture();
				})
				.executes(TeamProtectionCommand::toggleProtection));
	}

	private static int toggleProtection(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_TEAM.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to change protection settings")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String protectionName = StringArgumentType.getString(context, "protection_value").toUpperCase();
		TeamProtection protection;
		try {
			protection = TeamProtection.valueOf(protectionName);
		} catch (IllegalArgumentException e) {
			context.getSource().sendFailure(Component.literal(protectionName + " is not a valid protection.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!ProtectionManager.isTeamConfigurable(protection)) {
			context.getSource().sendFailure(Component.literal("The server does not allow teams to change '" + protection.getKey() + "'")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		boolean newState = database.toggleProtection(team, protection);

		context.getSource().sendSuccess(() -> Component.literal(protection.getKey() + " is now " + (newState ? "enabled" : "disabled"))
			.withStyle(ChatFormatting.GREEN), true);
		return 1;
	}
}
