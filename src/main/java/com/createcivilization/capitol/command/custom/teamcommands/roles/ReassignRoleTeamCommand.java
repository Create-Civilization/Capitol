package com.createcivilization.capitol.command.custom.teamcommands.roles;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.Text;

import java.util.*;

// WIP
public class ReassignRoleTeamCommand extends AbstractTeamCommand {

	public ReassignRoleTeamCommand() {
		super("reassignRole");
		command = CommandManager.literal(subCommandName)
			.requires(this::canExecuteAllParams)
			.then(CommandManager.argument("player", EntityArgumentType.players()))
			.then(CommandManager.argument("roleName", StringArgumentType.string()).executes(this::executeAllParams));
	}

	@Override
	public int executeAllParams(CommandContext<ServerCommandSource> context) {
		ServerCommandSource source = context.getSource();
		PlayerEntity player = source.getPlayer();
		PlayerEntity toPromote;
		try {
			toPromote = EntityArgumentType.getPlayer(context, "player");
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}

		Team team = TeamUtils.getTeam(player).getOrThrow();
		String role = StringArgumentType.getString(context,"roleName");

		if (!Objects.equals(TeamUtils.getTeam(player), TeamUtils.getTeam(toPromote))){
			source.sendError(Text.literal("Player is not from the same team as you"));
			return -1;
		}
		String finalRole = role;
		if (Arrays.stream(team.getRoles()).noneMatch(query -> Objects.equals(query.toLowerCase(), finalRole.toLowerCase()))){
			source.sendError(Text.literal("Role not found."));
			return -1;
		}
		for (String currRole : team.getRoles()) if (Objects.equals(currRole.toLowerCase(), finalRole)) role = currRole;
		assert player != null;
		if (TeamUtils.isRoleHigher(team, team.getPlayerRole(player.getUuid()), role)){
			source.sendError(Text.literal("Cannot reassign player to a higher role than yours"));
			return -1;
		}
		UUID toPromoteUUID = toPromote.getUuid();
		team.removePlayer(toPromoteUUID);
		team.addPlayer(role, toPromoteUUID);
		toPromote.sendMessage(Text.literal("You have been successfully reassigned to \"" + role + "\""));
		source.sendFeedback(() -> Text.literal("Successfully reassigned " + toPromote.getName()), true);
		return 1;
	}

	@Override
	public boolean canExecute(PlayerEntity player) {
		setMustWhat("be a player, be in a team and have role making permissions");
		return TeamUtils.hasTeam(player)
			&& TeamUtils.getPlayerPermission(TeamUtils.getTeam(player).getOrThrow(), player).editPermissions();
	}
}
