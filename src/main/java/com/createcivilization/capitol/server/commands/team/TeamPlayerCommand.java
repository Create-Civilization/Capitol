package com.createcivilization.capitol.server.commands.team;

import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamMember;
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
import java.util.Optional;

class TeamPlayerCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("player")
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
				.then(Commands.literal("permission")
					.then(Commands.literal("reset")
						.executes(TeamPlayerCommand::resetPermissions))
					.then(Commands.argument("permission_value", StringArgumentType.word())
						.suggests((context, builder) -> {
							for (Permission perm : Permission.values()) {
								builder.suggest(perm.name().toLowerCase());
							}
							return builder.buildFuture();
						})
						.executes(TeamPlayerCommand::togglePermission))));
	}

	private static int togglePermission(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to manage individual permissions")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String playerName = StringArgumentType.getString(context, "player");
		GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
		Optional<GameProfile> profile = profileCache.get(playerName);
		if (profile.isEmpty()) {
			context.getSource().sendFailure(Component.literal("There is no player called " + playerName).withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfile gameProfile = profile.get();

		if (!database.isPlayerInTeam(gameProfile.getId(), team)) {
			context.getSource().sendFailure(Component.literal(gameProfile.getName() + " is not a member of " + team.getName())
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

		long rolePerms = database.getPlayerPermission(gameProfile.getId(), team);
		Long individualPerms = database.getIndividualPermissions(gameProfile.getId(), team);
		long currentIndividual = individualPerms != null ? individualPerms : 0L;

		boolean oldState = permission.hasPermission(rolePerms | currentIndividual);
		long newIndividual = permission.toggle(currentIndividual);
		boolean newState = permission.hasPermission(rolePerms | newIndividual);

		database.setIndividualPermissions(gameProfile.getId(), team, newIndividual);

		context.getSource().sendSuccess(() -> Component.literal("Permission ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(permissionName.toLowerCase()).withStyle(ChatFormatting.AQUA))
			.append(Component.literal(" for ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(gameProfile.getName()).withStyle(ChatFormatting.WHITE))
			.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(String.valueOf(oldState)).withStyle(oldState ? ChatFormatting.GREEN : ChatFormatting.RED))
			.append(Component.literal(" → ").withStyle(ChatFormatting.DARK_GRAY))
			.append(Component.literal(String.valueOf(newState)).withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)), true);
		return 1;
	}

	private static int resetPermissions(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to manage individual permissions")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String playerName = StringArgumentType.getString(context, "player");
		GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
		Optional<GameProfile> profile = profileCache.get(playerName);
		if (profile.isEmpty()) {
			context.getSource().sendFailure(Component.literal("There is no player called " + playerName).withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfile gameProfile = profile.get();

		if (!database.isPlayerInTeam(gameProfile.getId(), team)) {
			context.getSource().sendFailure(Component.literal(gameProfile.getName() + " is not a member of " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		database.removeIndividualPermissions(gameProfile.getId(), team);

		context.getSource().sendSuccess(() -> Component.literal("Individual permissions for ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(gameProfile.getName()).withStyle(ChatFormatting.WHITE))
			.append(Component.literal(" reset to role defaults.").withStyle(ChatFormatting.GRAY)), true);
		return 1;
	}
}
