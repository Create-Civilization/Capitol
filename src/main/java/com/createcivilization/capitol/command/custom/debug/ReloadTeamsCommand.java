package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ReloadTeamsCommand extends AbstractDebugCommand {

	public ReloadTeamsCommand() {
		super();
		subSubCommand.set(
			Commands.literal("reloadTeams")
				.requires(this::canExecuteAllParams)
				.executes(this::executeAllParams)
		);
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and an operator");
		return player.hasPermissions(4);
	}

	@Override
	public int execute(Player player) {
		player.sendSystemMessage(Component.literal("Reloading teams..."));
		return TeamUtils.reloadTeams();
	}
}