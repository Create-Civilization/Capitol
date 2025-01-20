package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.Suggestions;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

import static com.createcivilization.capitol.util.TeamUtils.loadedTeams;

public class RemoveTeamDebugCommand extends AbstractDebugCommand {

	public RemoveTeamDebugCommand() {
		super();
		subSubCommand.set(
			Commands.literal("removeTeam")
				.requires(this::canExecuteAllParams)
				.then(
					Commands.argument("teamName", StringArgumentType.string())
						.suggests(Suggestions.TEAM_NAMES)
						.requires(this::canExecuteAllParams)
						.executes(this::executeAllParams)
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
		String teamName = StringArgumentType.getString(context, "teamName");
		if (loadedTeams.removeIf(team -> Objects.equals(team.getName(), teamName))) {
			context.getSource().sendSuccess(() -> Component.literal("Team \"" + teamName + "\" has been removed, please rejoin to update commands"), true);
			return 1;
		}
		context.getSource().sendSuccess(() -> Component.literal("No team \"" + teamName + "\" found"), true);
		return 1;
	}
}