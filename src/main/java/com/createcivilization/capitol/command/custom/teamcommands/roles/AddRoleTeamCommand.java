package com.createcivilization.capitol.command.custom.teamcommands.roles;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.team.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class AddRoleTeamCommand extends AbstractTeamCommand {

	public AddRoleTeamCommand() {
		super("addRole");
		command.set(
			Commands.literal(subCommandName.getOrThrow())
				.requires(this::canExecuteAllParams)
				.then(Commands.argument("roleName", StringArgumentType.string())
					.executes(this::executeAllParams))
		);
	}

	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context) {
		String roleName = StringArgumentType.getString(context, "roleName");
		TeamUtils.getTeam(context.getSource().getPlayer()).getOrThrow().addRole(roleName);
		context.getSource().sendSuccess(() -> Component.literal("Added role \"" + roleName + "\""), true);
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player, be in a receivingTeam and have role making permissions");
		return TeamUtils.hasTeam(player) && TeamUtils.getPlayerPermission(TeamUtils.getTeam(player).getOrThrow(), player).get("editRoles");
	}
}