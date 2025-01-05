package com.createcivilization.capitol.command.custom.teamcommands.roles;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.*;

// WIP
public class ReassignRoleTeamCommand extends AbstractTeamCommand {

	public ReassignRoleTeamCommand() {
		super("reassignRole");
		command.set(
			Commands.literal(subCommandName.getOrThrow())
			.requires(this::canExecuteAllParams)
			.then(Commands.argument("player", EntityArgument.players())
			.then(Commands.argument("roleName", StringArgumentType.string())
				.suggests(SUGGESTION_PROVIDER)
				.executes(this::executeAllParams)
			))
		);
	}

	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		Player player = source.getPlayer();
		Player toPromote;
		try {
			toPromote = EntityArgument.getPlayer(context, "player");
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
		if (Objects.equals(player, toPromote)) {
			source.sendFailure(Component.literal("Cannot reassign your own role"));
			return -1;
		}

		Team team = TeamUtils.getTeam(player).getOrThrow();
		String role = StringArgumentType.getString(context,"roleName");

		if (!Objects.equals(TeamUtils.getTeam(player).getOrThrow().getTeamId(), TeamUtils.getTeam(toPromote).getOrThrow().getTeamId())){
			source.sendFailure(Component.literal("Player is not from the same team as you"));
			return -1;
		}
		String finalRole = role;
		if (Arrays.stream(team.getRoles()).noneMatch(query -> Objects.equals(query.toLowerCase(), finalRole.toLowerCase()))){
			source.sendFailure(Component.literal("Role not found."));
			return -1;
		}
		for (String currRole : team.getRoles()) if (Objects.equals(currRole.toLowerCase(), finalRole)) role = currRole;
		assert player != null;
		if (TeamUtils.isRoleHigher(team, team.getPlayerRole(player.getUUID()), role)){
			source.sendFailure(Component.literal("Cannot reassign player to a higher role than yours"));
			return -1;
		}
		UUID toPromoteUUID = toPromote.getUUID();
		team.removePlayer(toPromoteUUID);
		team.addPlayer(role, toPromoteUUID);
		toPromote.sendSystemMessage(Component.literal("You have been successfully reassigned to the role \"" + role + "\""));
		String finalRole1 = role;
		source.sendSuccess(() -> Component.literal("Successfully reassigned \"" + toPromote.getName().getString() + "\" to \"" + finalRole1 + "\""), true);
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player, be in a team and have role making permissions");
		return TeamUtils.hasTeam(player)
			&& TeamUtils.getPlayerPermission(TeamUtils.getTeam(player).getOrThrow(), player).get("editPermissions");
	}

	private static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (context, builder) -> {
		CommandSourceStack source = context.getSource();
		Team team = TeamUtils.getTeam(source.getPlayer()).getOrThrow();
		String playerRole = team.getPlayerRole(Objects.requireNonNull(source.getPlayer()).getUUID());
		Arrays.stream(team.getRoles())
			.filter(role -> !TeamUtils.isRoleHigher(team, playerRole, role))
			.forEach(builder::suggest);
		return builder.buildFuture();
	};
}