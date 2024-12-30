package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.*;
import com.createcivilization.capitol.util.*;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.*;
import net.minecraft.text.Text;

public class ActivateWarTeamsDebugCommand extends AbstractTeamCommand {

	public ActivateWarTeamsDebugCommand() {
		super("startWar");
		command = CommandManager.literal("debug")
			.then(CommandManager.literal(subCommandName)
			.then(CommandManager.argument("attackerTeamName", StringArgumentType.string()))
			.then(CommandManager.argument("defenderTeamName", StringArgumentType.string()))
			.requires(this::canExecuteAllParams)
			.executes(this::executeAllParams));
	}

	@Override
	public boolean canExecuteAllParams(ServerCommandSource s) {
		setMustWhat("be a player and an operator");
		return s.hasPermissionLevel(4);
	}

	@Override
	public int executeAllParams(CommandContext<ServerCommandSource> context) {
		String attackerTeamName = StringArgumentType.getString(context, "attackerTeamName");
		String defenderTeamName = StringArgumentType.getString(context, "defenderTeamName");
		Team attackerTeam = TeamUtils.getTeam(attackerTeamName).getOrThrow();
		Team defenderTeam = TeamUtils.getTeam(defenderTeamName).getOrThrow();
		TeamUtils.wars.add(new War(attackerTeam, defenderTeam));

		context.getSource().sendFeedback(() -> Text.literal(
			"Successfully intiated a war between \"" + attackerTeamName + "\" and \"" + defenderTeamName + "\""
			), true);
		return 1;
	}
}