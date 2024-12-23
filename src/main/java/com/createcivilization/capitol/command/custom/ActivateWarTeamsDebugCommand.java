package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.team.War;
import com.createcivilization.capitol.util.*;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;

public class ActivateWarTeamsDebugCommand extends AbstractTeamCommand {

	public ActivateWarTeamsDebugCommand() {
		super("startWar");
		command = Commands.literal(commandName)
			.then(Commands.argument("attackerTeamName", StringArgumentType.string()))
			.then(Commands.argument("defenderTeamName", StringArgumentType.string()))
			.requires(this::canExecuteAllParams)
			.executes(this::executeAllParams);
	}

	@Override
	public boolean canExecuteAllParams(CommandSourceStack s) {
		setMustWhat("be a player and an operator");
		return s.hasPermission(4);
	}

	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context) {
		String attackerTeamName = StringArgumentType.getString(context, "attackerTeamName");
		String defenderTeamName = StringArgumentType.getString(context, "defenderTeamName");
		Team attackerTeam = TeamUtils.getTeam(attackerTeamName).getOrThrow();
		Team defenderTeam = TeamUtils.getTeam(defenderTeamName).getOrThrow();
		TeamUtils.wars.add(new War(attackerTeam, defenderTeam));

		context.getSource().sendSuccess(() -> Component.literal(
			"Successfully intiated a war between \"" + attackerTeamName + "\" and \"" + defenderTeamName + "\""
			), true);

		return 1;
	}
}