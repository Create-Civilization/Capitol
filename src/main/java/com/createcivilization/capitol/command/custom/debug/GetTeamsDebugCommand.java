package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.List;

import static com.createcivilization.capitol.util.TeamUtils.parseTeams;

public class GetTeamsDebugCommand extends AbstractTeamCommand {

	public GetTeamsDebugCommand() {
		super("getTeams");
		command = CommandManager.literal("debug")
			.then(CommandManager.literal(commandName)
			.requires(this::canExecuteAllParams)
			.executes(this::executeAllParams));
	}

	@Override
	public int execute(PlayerEntity player) {
		List<Team> teams;
		try {
			teams = parseTeams(FileUtils.getFileContents(TeamUtils.getTeamDataFile()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (Team team : teams) player.sendMessage(Text.literal(team.toString()));
		return 1;
	}

	@Override
	public boolean canExecute(PlayerEntity player) {
		setMustWhat("be a player and an operator");
		return player.hasPermissionLevel(4);
	}
}
