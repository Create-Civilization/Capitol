package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.Text;

public class ReloadTeamsCommand extends AbstractTeamCommand {

	public ReloadTeamsCommand() {
		super("reloadTeams");
		command = CommandManager.literal("debug")
			.then(CommandManager.literal(commandName)
			.requires(this::canExecuteAllParams)
			.executes(this::executeAllParams));
	}

	@Override
	public boolean canExecuteAllParams(ServerCommandSource s) {
		setMustWhat("be a player and an operator");
		return s.hasPermissionLevel(4);
	}

	@Override
	public int execute(PlayerEntity player) {
		player.sendMessage(Text.literal("Reloading teams..."));
		return TeamUtils.reloadTeams();
	}
}