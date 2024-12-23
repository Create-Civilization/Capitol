package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.util.List;

import static com.createcivilization.capitol.util.TeamUtils.parseTeams;

public class GetTeamsDebugCommand extends AbstractTeamCommand {

	public GetTeamsDebugCommand() {
		super("getTeams");
		command = Commands.literal(commandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
	}

	@Override
	public int execute(Player player) {
		List<Team> teams;
		try {
			teams = parseTeams(FileUtils.getFileContents(TeamUtils.getTeamDataFile()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (Team team : teams) player.sendSystemMessage(Component.literal("Name: \"" + team.getName() + "\" Id: \"" + team.getTeamId() + "\""));
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and an operator");
		return TeamUtils.hasTeam(player);
	}
}
