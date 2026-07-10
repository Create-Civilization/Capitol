package com.createcivilization.capitol.server.commands.team;

import com.createcivilization.capitol.common.data.*;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

class TeamRoleCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("role")
			.then(Commands.literal("create")
				.then(Commands.argument("role_name", StringArgumentType.word())
					.executes(TeamRoleCommand::createRole)))
			.then(Commands.literal("edit")
				.then(Commands.argument("role_name", StringArgumentType.word())
				.suggests((context, builder) -> {
					CapitolDatabase database = DatabaseManager.database;
					Team team = database.getPlayerTeam(context.getSource().getPlayer());
					if (team == null) {
						builder.suggest("YOU ARE NOT IN A TEAM");
						return builder.buildFuture();
					}
					List<TeamRole> roles = database.getTeamRoles(team);
					for (TeamRole role : roles) {
						builder.suggest(role.name());
					}
					return builder.buildFuture();
				})
				.then(Commands.literal("assign")
					.then(Commands.argument("player", StringArgumentType.string())
						.suggests((context, builder) -> {
							CapitolDatabase database = DatabaseManager.database;
							Team team = database.getPlayerTeam(context.getSource().getPlayer());
							if (team == null) {
								builder.suggest("YOU ARE NOT IN A TEAM");
								return builder.buildFuture();
							}
							List<TeamMember> members = database.getTeamMembers(team);
							GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
							for (TeamMember member : members) {
								profileCache.get(member.playerUUID()).ifPresent(
									gameProfile -> builder.suggest(gameProfile.getName())
								);
							}
							return builder.buildFuture();
						})
						.executes(TeamRoleCommand::assignRole)))
				.then(Commands.literal("permission")
					.then(Commands.argument("permission_value", StringArgumentType.word())
						.suggests((context, builder) -> {
							for (Permission perm : Permission.values()) {
								builder.suggest(perm.name().toLowerCase());
							}
							return builder.buildFuture();
						})
						.executes(TeamRoleCommand::editRolePerms)))
					.then(Commands.literal("list").executes(TeamRoleCommand::viewRolePermList))
				.then(Commands.literal("name")
					.then(Commands.argument("new_name", StringArgumentType.string())
						.executes(TeamRoleCommand::editRoleName)))
				.then(Commands.literal("remove")
					.executes(TeamRoleCommand::removeRole))));
	}

	private static int createRole(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		if (player == null) {
			return failCommand(context, "commands.capitol.command_source_was_console_error");
		}

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			return failCommand(context, "commands.capitol.not_in_team_error");
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			return failCommand(context, "commands.capitol.team.role.create.no_permission");
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		database.addRole(team, roleName, 0);
		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.team.role.create.success",
			Component.literal(roleName).withStyle(ChatFormatting.AQUA),
			Component.literal(team.getName()).withStyle(ChatFormatting.GOLD))
			.withStyle(ChatFormatting.GRAY), true);
		return 1;
	}

	private static int removeRole(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		if (player == null) {
			return failCommand(context, "commands.capitol.command_source_was_console_error");
		}

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			return failCommand(context, "commands.capitol.not_in_team_error");
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			return failCommand(context, "commands.capitol.team.role.no_manage_permission");
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if (Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)) {
			return failCommand(context, "commands.capitol.team.role.remove.owner");
		}

		if (Objects.equals(roleName, TeamRole.DEFAULT_ROLE_NAME)) {
			return failCommand(context, "commands.capitol.team.role.remove.default");
		}

		if (Objects.equals(roleName, TeamRole.MEMBER_ROLE_NAME)) {
			return failCommand(context, "commands.capitol.team.role.remove.member");
		}

		TeamRole role = database.getRoleByName(team, roleName);

		if (role == null) {
			return failCommand(context, "commands.capitol.team.role.not_found", roleName, team.getName());
		}

		database.deleteRole(team, roleName);

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.team.role.remove.success",
			Component.literal(roleName).withStyle(ChatFormatting.AQUA))
			.withStyle(ChatFormatting.GRAY), true);
		return 1;
	}

	private static int assignRole(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		if (player == null) {
			return failCommand(context, "commands.capitol.command_source_was_console_error");
		}

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			return failCommand(context, "commands.capitol.not_in_team_error");
		}

		if (!Permission.ASSIGN_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			return failCommand(context, "commands.capitol.team.role.assign.no_permission");
		}

		String roleName = StringArgumentType.getString(context, "role_name");
		String playerName = StringArgumentType.getString(context, "player");

		if (Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)) {
			return failCommand(context, "commands.capitol.team.role.assign.owner");
		}

		TeamRole role = database.getRoleByName(team, roleName);
		if (role == null) {
			return failCommand(context, "commands.capitol.team.role.not_found", roleName, team.getName());
		}

		GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
		Optional<GameProfile> profile = profileCache.get(playerName);
		if (profile.isEmpty()) {
			return failCommand(context, "commands.capitol.no_player_found", playerName);
		}

		GameProfile gameProfile = profile.get();

		List<TeamMember> members = database.getTeamMembers(team);
		boolean isMember = members.stream().anyMatch(m -> m.playerUUID().equals(gameProfile.getId()));
		if (!isMember) {
			return failCommand(context, "commands.capitol.player_not_in_team", gameProfile.getName(), team.getName());
		}

		database.updatePlayerRole(gameProfile.getId(), team, role);

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.team.role.assign.success",
			Component.literal(gameProfile.getName()).withStyle(ChatFormatting.WHITE),
			Component.literal(roleName).withStyle(ChatFormatting.AQUA))
			.withStyle(ChatFormatting.GRAY), true);
		return 1;
	}

	private static int editRoleName(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		if (player == null) {
			return failCommand(context, "commands.capitol.command_source_was_console_error");
		}

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			return failCommand(context, "commands.capitol.not_in_team_error");
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			return failCommand(context, "commands.capitol.team.role.no_manage_permission");
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if (Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)) {
			return failCommand(context, "commands.capitol.team.role.edit.no_owner");
		}

		if (Objects.equals(roleName, TeamRole.DEFAULT_ROLE_NAME)) {
			return failCommand(context, "commands.capitol.team.role.edit.no_default");
		}

		TeamRole role = database.getRoleByName(team, roleName);

		if (role == null) {
			return failCommand(context, "commands.capitol.team.role.not_found", roleName, team.getName());
		}

		String newRoleName = StringArgumentType.getString(context, "new_name");

		database.updateRoleName(team, roleName, newRoleName);

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.team.role.name.success",
			Component.literal(roleName).withStyle(ChatFormatting.AQUA),
			Component.literal(newRoleName).withStyle(ChatFormatting.AQUA))
			.withStyle(ChatFormatting.GRAY), true);
		return 1;
	}

	private static int editRolePerms(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			return failCommand(context, "commands.capitol.not_in_team_error");
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			return failCommand(context, "commands.capitol.team.role.no_manage_permission");
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if (Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)) {
			return failCommand(context, "commands.capitol.team.role.edit.no_owner");
		}

		TeamRole role = database.getRoleByName(team, roleName);

		if (role == null) {
			return failCommand(context, "commands.capitol.team.role.not_found", roleName, team.getName());
		}

		String permissionName = StringArgumentType.getString(context, "permission_value").toUpperCase();
		Permission permission;
		try {
			permission = Permission.valueOf(permissionName);
		} catch (IllegalArgumentException e) {
			return failCommand(context, "commands.capitol.invalid_permission", permissionName);
		}

		long rolePerms = role.permissions();
		boolean oldState = permission.hasPermission(rolePerms);
		rolePerms = permission.toggle(rolePerms);
		boolean newState = permission.hasPermission(rolePerms);

		database.updateRolePermissions(team, roleName, rolePerms);

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.permission_toggle",
			Component.literal(permissionName.toLowerCase()).withStyle(ChatFormatting.AQUA),
			Component.literal(roleName).withStyle(ChatFormatting.GOLD),
			Component.literal(String.valueOf(oldState)).withStyle(oldState ? ChatFormatting.GREEN : ChatFormatting.RED),
			Component.literal(String.valueOf(newState)).withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED))
			.withStyle(ChatFormatting.GRAY), true);
		return 1;
	}

	private static int viewRolePermList(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		ServerPlayer player = context.getSource().getPlayer();
		if (player == null) {
			return failCommand(context, "commands.capitol.not_player");
		}
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			return failCommand(context, "commands.capitol.not_in_team_error");
		}

		String roleName = StringArgumentType.getString(context, "role_name");
		TeamRole role = database.getRoleByName(team, roleName);

		if (role == null) {
			return failCommand(context, "commands.capitol.team.role.not_found", roleName, team.getName());
		}

		context.getSource().sendSuccess(() ->
				Component.translatable("commands.capitol.team.role.permission_list.role").append(roleName).withStyle(ChatFormatting.GOLD)
			, false);

		for (Permission perm : Permission.values()) {
			MutableComponent comp = Component.literal("[").append(perm.hasPermission(role.permissions()) ? Component.literal("✔").withStyle(ChatFormatting.GREEN) : Component.literal("❌").withStyle(ChatFormatting.RED)).append("]");
			comp.withStyle(s -> s.withClickEvent(
				new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/capitol team role edit %s permission %s", roleName, perm.name().toLowerCase()))
			).withHoverEvent(
				new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("commands.capitol.team.role.permission_list.hover"))
			));
			context.getSource().sendSuccess(() ->
					comp.append(Component.literal(" ").withStyle(ChatFormatting.GOLD)).append(Component.literal(perm.name()).withStyle(ChatFormatting.WHITE))
				, false);
		}

		/*context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.permission_toggle",
				Component.literal(permissionName.toLowerCase()).withStyle(ChatFormatting.AQUA),
				Component.literal(roleName).withStyle(ChatFormatting.GOLD),
				Component.literal(String.valueOf(oldState)).withStyle(oldState ? ChatFormatting.GREEN : ChatFormatting.RED),
				Component.literal(String.valueOf(newState)).withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED))
			.withStyle(ChatFormatting.GRAY), true);*/
		return 1;
	}

	static int failCommand(CommandContext<CommandSourceStack> context, String key, Object... args) {
		context.getSource().sendFailure(Component.translatable(key, args).withStyle(ChatFormatting.RED));
		return 0;
	}
}