package com.createcivilization.capitol.command.custom.teamcommands.roles;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.Text;

// WIP
public class addRoleTeamCommand extends AbstractTeamCommand {
	public addRoleTeamCommand() {
		super("addRole");
		command = CommandManager.literal(commandName)
			.requires(this::canExecuteAllParams)
			.then(CommandManager.argument("roleName", StringArgumentType.string()).executes(this::executeAllParams)
		);
	}

	@Override
	public int executeAllParams(CommandContext<ServerCommandSource> context) {
		String roleName = StringArgumentType.getString(context, "roleName");
		TeamUtils.getTeam(context.getSource().getPlayer()).getOrThrow().addRole(roleName);
		context.getSource().sendFeedback(() -> Text.literal("Added role \"" + roleName + "\""), true);
		return 1;
	}

	@Override
	public boolean canExecute(PlayerEntity player) {
		setMustWhat("be a player, be in a team and have role making permissions");
		return TeamUtils.hasTeam(player) && TeamUtils.getPlayerPermission(TeamUtils.getTeam(player).getOrThrow(), player).addRole();
	}
}
