package com.createcivilization.capitol.command.custom.teamcommands.roles;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.*;

// WIP
public class EditRoleTeamCommand extends AbstractTeamCommand {

	public EditRoleTeamCommand() {
		super("editRole");
		command = Commands.literal(subCommandName)
			.requires(this::canExecuteAllParams)
			.then(Commands.argument("roleName", StringArgumentType.string())
				.suggests(SUGGESTION_PROVIDER_ROLES).then(
			Commands.argument("permission", StringArgumentType.string())
				.suggests(SUGGESTION_PROVIDER_PERMISSIONS).then(
			Commands.argument("value", BoolArgumentType.bool())
				.executes(this::executeAllParams)
				)
			)
		);
	}

	// TODO: Optimize value checks
	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		Player player = source.getPlayer();
		Team team = TeamUtils.getTeam(player).getOrThrow();
		String roleName = StringArgumentType.getString(context, "roleName");

		if (Arrays.stream(team.getRoles()).noneMatch(role -> Objects.equals(role.toLowerCase(), roleName.toLowerCase()))) {
			source.sendFailure(Component.literal("Invalid Role"));
			return -1;
		}

		String permission = StringArgumentType.getString(context, "permission");

		if (PermissionUtil.permissions.stream().noneMatch(perm -> Objects.equals(perm.toLowerCase(),  permission.toLowerCase()))) {
			source.sendFailure(Component.literal("Invalid permission"));
			return -1;
		}

		Boolean value = BoolArgumentType.getBool(context, "value");

		team.setPermission(roleName, permission, value);
		source.sendSuccess(() -> Component.literal("Successfully changed \"" + permission + "\" to " + value + "for \"" + roleName + "\""), true);

		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player, be in a team and have role making permissions");
		return TeamUtils.hasTeam(player) && TeamUtils.getPlayerPermission(TeamUtils.getTeam(player).getOrThrow(), player).get("editPermissions");
	}

	private static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER_PERMISSIONS = (context, builder) -> {
		PermissionUtil.permissions.forEach(builder::suggest);
		return builder.buildFuture();
	};
	private static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER_ROLES = (context, builder) -> {
		CommandSourceStack source = context.getSource();
		Team team = TeamUtils.getTeam(source.getPlayer()).getOrThrow();
		String playerRole = team.getPlayerRole(Objects.requireNonNull(source.getPlayer()).getUUID());
		Arrays.stream(team.getRoles())
			.filter(role -> !TeamUtils.isRoleHigher(team, playerRole, role))
			.forEach(builder::suggest);
		return builder.buildFuture();
	};
}