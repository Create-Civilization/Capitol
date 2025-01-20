package com.createcivilization.capitol.command.custom.debug;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class AdminModeCommand extends AbstractDebugCommand {

	public AdminModeCommand() {
		super();
		subSubCommand.set(
			Commands.literal("adminMode")
				.requires(this::canExecuteAllParams)
				.executes(this::executeAllParams)
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