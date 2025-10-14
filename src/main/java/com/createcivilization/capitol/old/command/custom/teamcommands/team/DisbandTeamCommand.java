package com.createcivilization.capitol.old.command.custom.teamcommands.team;

import com.createcivilization.capitol.old.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.old.util.team.TeamUtils;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class DisbandTeamCommand extends AbstractTeamCommand {

	public DisbandTeamCommand() {
		super("disbandTeam");
		command.set(Commands.literal(subCommandName.getOrThrow()).requires(this::canExecuteAllParams).executes(this::executeAllParams));
	}

	@Override
	public int execute(Player player) {
		// Only runs if player owns the receivingTeam (check canExecute)
		TeamUtils.removeTeam(TeamUtils.getTeam(player).getOrThrow().getTeamId());
		player.sendSystemMessage(Component.literal("Team successfully disbanded"));
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and own a receivingTeam");
		return TeamUtils.hasTeam(player) && TeamUtils.isTeamOwner(player);
	}
}