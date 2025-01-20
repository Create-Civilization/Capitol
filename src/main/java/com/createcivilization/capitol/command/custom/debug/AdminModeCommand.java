package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.custom.abstracts.AbstractPlayerCommand;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class AdminModeCommand extends AbstractPlayerCommand {

	public AdminModeCommand() {
		super("capitolTeams", "adminMode");
		command.set(
			Commands.literal("debug")
				.requires(this::canExecuteAllParams)
				.then(Commands.literal(subCommandName.getOrThrow())
					.executes(this::executeAllParams)
				)
		);
	}

	@Override
	public boolean canExecute(Player player) {
		return player.hasPermissions(4);
	}

	@Override
	public int execute(Player player) {
		var data = player.getPersistentData();
		if (!data.contains("capitolTeamsAdminMode")) data.putBoolean("capitolTeamsAdminMode", true);
		else data.putBoolean("capitolTeamsAdminMode", !data.getBoolean("capitolTeamsAdminMode"));
		player.sendSystemMessage(Component.literal("Admin mode " + (data.getBoolean("capitolTeamsAdminMode") ? "enabled" : "disabled")));
		return 1;
	}
}