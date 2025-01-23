package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.Suggestions;
import com.createcivilization.capitol.team.*;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ActivateWarTeamsDebugCommand extends AbstractDebugCommand {

	public ActivateWarTeamsDebugCommand() {
		super();
		subSubCommand.set(
			Commands.literal("startWar")
				.then(
					Commands.argument("attackerTeamName", StringArgumentType.string())
						.suggests(Suggestions.TEAM_NAMES)
						.then(
							Commands.argument("defenderTeamName", StringArgumentType.string())
								.suggests(Suggestions.TEAM_NAMES)
								.requires(this::canExecuteAllParams)
								.executes(this::executeAllParams)
						)
				)

		);
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and an operator");
		return player.hasPermissions(4);
	}

	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context) {
		String attackerTeamName = StringArgumentType.getString(context, "attackerTeamName");
		String defenderTeamName = StringArgumentType.getString(context, "defenderTeamName");
		Team attackerTeam = TeamUtils.getTeamByName(attackerTeamName).getOrThrow();
		Team defenderTeam = TeamUtils.getTeamByName(defenderTeamName).getOrThrow();
		TeamUtils.wars.add(new War(attackerTeam, defenderTeam));

		context.getSource().sendSuccess(() -> Component.literal(
			"Successfully intiated a war between \"" + attackerTeamName + "\" and \"" + defenderTeamName + "\""
			), true
		);

		return 1;
	}
}