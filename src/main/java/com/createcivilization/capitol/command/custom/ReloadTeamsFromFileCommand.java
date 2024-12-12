package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ReloadTeamsFromFileCommand extends AbstractTeamCommand {

	public ReloadTeamsFromFileCommand() {
		super("reloadTeamsFromFile");
		command = Commands.literal(commandName).requires(this::canExecuteAllParams)
			.executes(this::executeAllParams);
	}

	@Override
	public boolean canExecuteAllParams(CommandSourceStack s) {
		setMustWhat("be a player and an operator");
		return s.hasPermission(4);
	}

	@Override
	public int execute(Player player) {
		player.sendSystemMessage(Component.literal("Reloading teams from file..."));
		return TeamUtils.reloadTeamsFromFile();
	}
}