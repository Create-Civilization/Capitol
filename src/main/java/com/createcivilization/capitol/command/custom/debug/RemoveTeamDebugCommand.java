package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.*;
import net.minecraft.text.Text;

import java.util.Objects;

import static com.createcivilization.capitol.util.TeamUtils.loadedTeams;

public class RemoveTeamDebugCommand extends AbstractTeamCommand {

	public RemoveTeamDebugCommand() {
		super("removeTeam");
		command = CommandManager.literal("debug")
			.then(CommandManager.literal(subCommandName)
			.then(CommandManager.argument("teamName", StringArgumentType.string())
			.requires(this::canExecuteAllParams)
			.executes(this::executeAllParams)));
	}

	@Override
	public boolean canExecuteAllParams(ServerCommandSource s) {
		setMustWhat("be a player and an operator");
		return s.hasPermissionLevel(4);
	}

	@Override
	public int executeAllParams(CommandContext<ServerCommandSource> context) {
		String teamName = StringArgumentType.getString(context, "teamName");
		if (loadedTeams.removeIf(team -> Objects.equals(team.getName(), teamName))) {
			context.getSource().sendFeedback(() -> Text.literal("Team \"" + teamName + "\" has been removed, please rejoin to update commands"), true);
			return 1;
		}
		context.getSource().sendError(Text.literal("No team \"" + teamName + "\" found"));
		return 1;
	}
}
