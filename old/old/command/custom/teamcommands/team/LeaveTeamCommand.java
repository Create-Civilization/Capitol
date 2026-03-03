package com.createcivilization.capitol.old.old.command.custom.teamcommands.team;

import com.createcivilization.capitol.old.old.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class LeaveTeamCommand extends AbstractTeamCommand {

	public LeaveTeamCommand() {
		super("leaveTeam");
		command.set(
			Commands.literal(subCommandName.getOrThrow())
				.requires(this::canExecuteAllParams)
				.executes(this::executeAllParams)
		);
	}

	@Override
	public int execute(Player player) {
		OldTeam playerOldTeam = TeamUtils.getTeam(player).getOrThrow();
		List<UUID> owners = playerOldTeam.getPlayersWithRole("owner");
		if (owners.contains(player.getUUID()) && owners.size() == 1){
			player.sendSystemMessage(Component.literal("You cannot leave the receivingOldTeam as the only owner"));
			return -1;
		}
		playerOldTeam.removePlayer(player.getUUID());
		player.sendSystemMessage(Component.literal("Successfully left receivingOldTeam"));
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and in a receivingOldTeam");
		return TeamUtils.hasTeam(player);
	}
}