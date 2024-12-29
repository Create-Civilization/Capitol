package com.createcivilization.capitol.command.custom.teamcommands.team;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import static com.createcivilization.capitol.util.TeamUtils.loadedTeams;

public class DisbandTeamCommand extends AbstractTeamCommand {

	public DisbandTeamCommand() {
		super("disbandTeam");
		command = CommandManager.literal(commandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
	}

	@Override
	public int execute(PlayerEntity player) {
		// Only runs if player owns the team (check canExecute)
		loadedTeams.remove(TeamUtils.getTeam(player).get());
		player.sendMessage(Text.literal("Team successfully disbanded"));
		return 1;
	}

	@Override
	public boolean canExecute(PlayerEntity player) {
		setMustWhat("be a player and own a team");
		return TeamUtils.hasTeam(player) && TeamUtils.isTeamOwner(player);
	}
}
