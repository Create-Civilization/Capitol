package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.FileUtils;
import com.createcivilization.capitol.util.TeamUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import wiiu.mavity.util.ObjectHolder;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.createcivilization.capitol.util.TeamUtils.loadedTeams;
import static com.createcivilization.capitol.util.TeamUtils.parseTeams;

public class RemoveTeamDebugCommand extends AbstractTeamCommand {
	public RemoveTeamDebugCommand() {
		super("removeTeam");
		command = Commands.literal(commandName).then(Commands.argument("teamName", StringArgumentType.string()).requires(this::canExecuteAllParams).executes(this::executeAllParams));
	}

	@Override
	public boolean canExecuteAllParams(CommandSourceStack s) {
		setMustWhat("be a player and an operator");
		return s.hasPermission(4);
	}

	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context)
	{
		String teamName = StringArgumentType.getString(context, "teamName");
		if (loadedTeams.removeIf(team -> Objects.equals(team.getName(), teamName)))
		{
			context.getSource().sendSuccess(() -> Component.literal("Team \"" + teamName + "\" has been removed, please rejoin to update commands"), true);
			return 1;
		}
		context.getSource().sendSuccess(() -> Component.literal("No team \"" + teamName + "\" found"), true);
		return 0;
	}
}
