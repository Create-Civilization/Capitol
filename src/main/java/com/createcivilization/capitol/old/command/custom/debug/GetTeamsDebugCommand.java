package com.createcivilization.capitol.old.command.custom.debug;

import com.createcivilization.capitol.old.team.Team;
import com.createcivilization.capitol.old.util.data.DataManager;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class GetTeamsDebugCommand extends AbstractDebugCommand {

	public GetTeamsDebugCommand() {
		super();
		subSubCommand.set(
			Commands.literal("getTeams")
				.requires(this::canExecuteAllParams)
				.executes(this::executeAllParams)
		);
	}

	@Override
	public int execute(Player player) {
		if (DataManager.TeamData.loadedTeams.isEmpty()) player.sendSystemMessage(Component.literal("No teams loaded"));
		for (Team team : DataManager.TeamData.loadedTeams) player.sendSystemMessage(Component.literal(team.toString()));
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and an operator");
		return player.hasPermissions(4);
	}
}