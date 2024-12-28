package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;

import static com.createcivilization.capitol.util.TeamUtils.loadedTeams;

public class LeaveTeamCommand extends AbstractTeamCommand {
	public LeaveTeamCommand() {
		super("leaveTeam");
		command = Commands.literal(commandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
	}

	@Override
	public int execute(Player player)
	{
		Team playerTeam = TeamUtils.getTeam(player).getOrThrow();
		List<UUID> owners = playerTeam.getPlayersWithRole("owner");
		if (owners.contains(player.getUUID()) && owners.size() == 1){
			player.sendSystemMessage(Component.literal("You cannot leave the team as the only owner"));
			return -1;
		}
		playerTeam.removePlayer(player.getUUID());
		player.sendSystemMessage(Component.literal("Successfully left team"));
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and in a team");
		return TeamUtils.hasTeam(player);
	}
}