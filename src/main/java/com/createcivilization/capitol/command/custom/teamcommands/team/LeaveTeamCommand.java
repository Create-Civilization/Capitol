package com.createcivilization.capitol.command.custom.teamcommands.team;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.*;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import java.util.*;

public class LeaveTeamCommand extends AbstractTeamCommand {
	public LeaveTeamCommand() {
		super("leaveTeam");
		command = CommandManager.literal(commandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
	}

	@Override
	public int execute(PlayerEntity player)
	{
		Team playerTeam = TeamUtils.getTeam(player).getOrThrow();
		List<UUID> owners = playerTeam.getPlayersWithRole("owner");
		if (owners.contains(player.getUuid()) && owners.size() == 1){
			player.sendMessage(Text.literal("You cannot leave the team as the only owner"));
			return -1;
		}
		playerTeam.removePlayer(player.getUuid());
		player.sendMessage(Text.literal("Successfully left team"));
		return 1;
	}

	@Override
	public boolean canExecute(PlayerEntity player) {
		setMustWhat("be a player and in a team");
		return TeamUtils.hasTeam(player);
	}
}
