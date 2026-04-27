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
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.GameProfileCache;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

class TeamRoleCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("role")
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
				.then(Commands.literal("create")
					.executes(TeamRoleCommand::createRole))
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
				.then(Commands.literal("name")
					.then(Commands.argument("new_name", StringArgumentType.string())
						.executes(TeamRoleCommand::editRoleName)))
				.then(Commands.literal("remove")
					.executes(TeamRoleCommand::removeRole)));
	}

	private static int createRole(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to create a role")
				.withStyle(ChatFormatting.RED));
			return 0;
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		database.addRole(team, roleName, 0);
		context.getSource().sendSuccess(() -> Component.literal("Role ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(roleName).withStyle(ChatFormatting.AQUA))
			.append(Component.literal(" added to ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(team.getName()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}

	private static int removeRole(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to edit this role")
				.withStyle(ChatFormatting.RED));
			return 0;
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if (Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)) {
			context.getSource().sendFailure(Component.literal("You cannot remove owner role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		if (Objects.equals(roleName, TeamRole.DEFAULT_ROLE_NAME)) {
			context.getSource().sendFailure(Component.literal("You cannot remove default role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		TeamRole role = database.getRoleByName(team, roleName);

		if (role == null) {
			context.getSource().sendFailure(Component.literal("The role " + roleName + " does not exist in " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		database.deleteRole(team, roleName);

		context.getSource().sendSuccess(() -> Component.literal("Deleted role ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(roleName).withStyle(ChatFormatting.AQUA)), true);
		return 1;
	}

	private static int assignRole(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.ASSIGN_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to assign roles")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String roleName = StringArgumentType.getString(context, "role_name");
		String playerName = StringArgumentType.getString(context, "player");

		if (Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)) {
			context.getSource().sendFailure(Component.literal("You cannot assign the owner role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		TeamRole role = database.getRoleByName(team, roleName);
		if (role == null) {
			context.getSource().sendFailure(Component.literal("The role " + roleName + " does not exist in " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
		Optional<GameProfile> profile = profileCache.get(playerName);
		if (profile.isEmpty()) {
			context.getSource().sendFailure(Component.literal("There is no player called " + playerName)
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfile gameProfile = profile.get();

		List<TeamMember> members = database.getTeamMembers(team);
		boolean isMember = members.stream().anyMatch(m -> m.playerUUID().equals(gameProfile.getId()));
		if (!isMember) {
			context.getSource().sendFailure(Component.literal(gameProfile.getName() + " is not a member of " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		database.updatePlayerRole(gameProfile.getId(), team, role);

		context.getSource().sendSuccess(() -> Component.literal("Assigned ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(gameProfile.getName()).withStyle(ChatFormatting.WHITE))
			.append(Component.literal(" the role ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(roleName).withStyle(ChatFormatting.AQUA)), true);
		return 1;
	}

	private static int editRoleName(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to edit this role")
				.withStyle(ChatFormatting.RED));
			return 0;
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if (Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)) {
			context.getSource().sendFailure(Component.literal("You cannot edit owner role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		TeamRole role = database.getRoleByName(team, roleName);

		if (role == null) {
			context.getSource().sendFailure(Component.literal("The role " + roleName + " does not exist in " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String newRoleName = StringArgumentType.getString(context, "new_name");

		database.updateRoleName(team, roleName, newRoleName);

		context.getSource().sendSuccess(() -> Component.literal("Renamed role ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(roleName).withStyle(ChatFormatting.AQUA))
			.append(Component.literal(" → ").withStyle(ChatFormatting.DARK_GRAY))
			.append(Component.literal(newRoleName).withStyle(ChatFormatting.AQUA)), true);
		return 1;
	}

	private static int editRolePerms(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to edit this role")
				.withStyle(ChatFormatting.RED));
			return 0;
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if (Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)) {
			context.getSource().sendFailure(Component.literal("You cannot edit owner role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		TeamRole role = database.getRoleByName(team, roleName);

		if (role == null) {
			context.getSource().sendFailure(Component.literal("The role " + roleName + " does not exist in " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String permissionName = StringArgumentType.getString(context, "permission_value").toUpperCase();
		Permission permission;
		try {
			permission = Permission.valueOf(permissionName);
		} catch (IllegalArgumentException e) {
			context.getSource().sendFailure(Component.literal(permissionName + " is not a valid permission.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		long rolePerms = role.permissions();
		boolean oldState = permission.hasPermission(rolePerms);
		rolePerms = permission.toggle(rolePerms);
		boolean newState = permission.hasPermission(rolePerms);

		database.updateRolePermissions(team, roleName, rolePerms);

		context.getSource().sendSuccess(() -> Component.literal("Permission ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(permissionName.toLowerCase()).withStyle(ChatFormatting.AQUA))
			.append(Component.literal(" for ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(roleName).withStyle(ChatFormatting.GOLD))
			.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(String.valueOf(oldState)).withStyle(oldState ? ChatFormatting.GREEN : ChatFormatting.RED))
			.append(Component.literal(" → ").withStyle(ChatFormatting.DARK_GRAY))
			.append(Component.literal(String.valueOf(newState)).withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)), true);
		return 1;
	}
}