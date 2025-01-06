package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.Suggestions;
import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.*;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;

public class ActivateWarTeamsDebugCommand extends AbstractTeamCommand {

	public ActivateWarTeamsDebugCommand() {
		super("startWar");
		command.set(
			Commands.literal("debug")
				.then(Commands.literal(subCommandName.getOrThrow())
					.then(Commands.argument("attackerTeamName", StringArgumentType.string()).suggests(Suggestions.TEAM_NAME)
						.then(Commands.argument("defenderTeamName", StringArgumentType.string()).suggests(Suggestions.TEAM_NAME))
					)
					.requires(this::canExecuteAllParams)
					.executes(this::executeAllParams)
				)
		);
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
			), true
		);

		return 1;
	}
}