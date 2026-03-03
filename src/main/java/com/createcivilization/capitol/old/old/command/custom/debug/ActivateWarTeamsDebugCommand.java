package com.createcivilization.capitol.old.old.command.custom.debug;

import com.createcivilization.capitol.old.old.command.Suggestions;
import com.createcivilization.capitol.old.old.team.*;
import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;

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
		DataManager.WarData.loadedWars.add(new War(attackerTeam, defenderTeam));

		context.getSource().sendSuccess(() -> Component.literal(
			"Successfully intiated a war between \"" + attackerTeamName + "\" and \"" + defenderTeamName + "\""
			), true
		);

		return 1;
	}
}