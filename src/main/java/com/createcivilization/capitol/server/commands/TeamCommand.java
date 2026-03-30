package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.common.data.*;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.awt.*;
import java.util.*;
import java.util.List;

public class TeamCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("team")
			.then(Commands.literal("create")
				.then(Commands.argument("name", StringArgumentType.string())
					.then(Commands.argument("tag", StringArgumentType.word())
						.then(Commands.argument("color", StringArgumentType.word())
							.then(Commands.argument("description", StringArgumentType.string())
								.executes(TeamCommand::createTeam))
							.executes(TeamCommand::createTeam)))))
			.then(Commands.literal("info").executes(TeamCommand::teamInfo))
			.then(Commands.literal("manage")
				.then(Commands.literal("roles")
					.then(Commands.literal("create")
						.then(Commands.argument("role_name", StringArgumentType.string())
							.executes(TeamCommand::createRole)))
					.then(Commands.literal("edit")
						.then(Commands.argument("role_name", StringArgumentType.string())
							.suggests((context, builder) -> {
								CapitolDatabase database = DatabaseManager.database;
								Team team = database.getPlayerTeam(context.getSource().getPlayer());
								if(team == null){
									builder.suggest("YOU ARE NOT IN A TEAM");
									return builder.buildFuture();
								}
								List<TeamRole> roles = database.getTeamRoles(team);
								for(TeamRole role : roles){
									builder.suggest(role.name());
								}
								return builder.buildFuture();
							})
							.then(Commands.literal("permission")
								.then(Commands.argument("permission", StringArgumentType.string())
									.suggests(((context, builder) -> {
										for(Permission perm : Permission.values()){
											builder.suggest(perm.name());
										}
										return builder.buildFuture();
									}))
									.executes(TeamCommand::editRolePerms)))
							.then(Commands.literal("name")
								.then(Commands.argument("new_name", StringArgumentType.string())
									.executes(TeamCommand::editRoleName)))))
					.then(Commands.literal("remove")
						.then(Commands.argument("role_name", StringArgumentType.string())
							.suggests((context, builder) -> {
								CapitolDatabase database = DatabaseManager.database;
								Team team = database.getPlayerTeam(context.getSource().getPlayer());
								if(team == null){
									builder.suggest("YOU ARE NOT IN A TEAM");
									return builder.buildFuture();
								}
								List<TeamRole> roles = database.getTeamRoles(team);
								for(TeamRole role : roles){
									builder.suggest(role.name());
								}
								return builder.buildFuture();
							})
							.executes(TeamCommand::removeRole)))
			.then(Commands.literal("disband")
				.executes(TeamCommand::promptDeleteTeam))
			.then(Commands.literal("kick")
				.then(Commands.argument("player", StringArgumentType.string())
					.suggests(((context, builder) ->{
						CapitolDatabase database = DatabaseManager.database;
						Team team = database.getPlayerTeam(context.getSource().getPlayer());
						if(team == null){
							builder.suggest("YOU ARE NOT IN A TEAM");
							return builder.buildFuture();
						}
						List<TeamMember> members = database.getTeamMembers(team);
						GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
						for (TeamMember member : members){
							profileCache.get(member.playerUUID()).ifPresent(
								gameProfile -> builder.suggest(gameProfile.getName())
							);
						}
						return builder.buildFuture();
					}))
					.executes(TeamCommand::kickPlayer)))
			.then(Commands.literal("confirm_disband")
				.executes(TeamCommand::confirmDeleteTeam))));
	}

	private static int removeRole(CommandContext<CommandSourceStack> context){
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if(team == null){
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if(!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))){
			context.getSource().sendFailure(Component.literal("You do not have permission to edit this role")
				.withStyle(ChatFormatting.RED));
			return 0;
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if(Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)){
			context.getSource().sendFailure(Component.literal("You cannot remove owner role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		if(Objects.equals(roleName, TeamRole.DEFAULT_ROLE_NAME)){
			context.getSource().sendFailure(Component.literal("You cannot remove default role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}


		TeamRole role = database.getRoleByName(team, roleName);

		if(role == null){
			context.getSource().sendFailure(Component.literal("The role " + roleName + " does not exist in " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		database.deleteRole(team, roleName);


		context.getSource().sendSuccess(() -> Component.literal("Deleted Role: " + roleName).withStyle(ChatFormatting.RED), true);
		return 1;

	}

	private static int editRoleName(CommandContext<CommandSourceStack> context){
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if(team == null){
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if(!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))){
			context.getSource().sendFailure(Component.literal("You do not have permission to edit this role")
				.withStyle(ChatFormatting.RED));
			return 0;
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if(Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)){
			context.getSource().sendFailure(Component.literal("You cannot edit owner role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		TeamRole role = database.getRoleByName(team, roleName);

		if(role == null){
			context.getSource().sendFailure(Component.literal("The role " + roleName + " does not exist in " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String newRoleName = StringArgumentType.getString(context, "new_name");

		database.updateRoleName(team, roleName, newRoleName);

		context.getSource().sendSuccess(() -> Component.literal("Updated Role: " + roleName + ". New Role Name: " + newRoleName + ".").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int editRolePerms(CommandContext<CommandSourceStack> context){
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if(team == null){
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if(!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))){
			context.getSource().sendFailure(Component.literal("You do not have permission to edit this role")
				.withStyle(ChatFormatting.RED));
			return 0;
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		if(Objects.equals(roleName, TeamRole.OWNER_ROLE_NAME)){
			context.getSource().sendFailure(Component.literal("You cannot edit owner role.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		TeamRole role = database.getRoleByName(team, roleName);

		if(role == null){
			context.getSource().sendFailure(Component.literal("The role " + roleName + " does not exist in " + team.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String permissionName = StringArgumentType.getString(context, "permission");
		Permission permission;
		try {
			permission = Permission.valueOf(permissionName);
		} catch (IllegalArgumentException e){
			context.getSource().sendFailure(Component.literal(permissionName + "is not a valid permission.")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		int rolePerms = role.permissions();
		boolean oldState = permission.hasPermission(rolePerms);
		rolePerms = permission.toggle(rolePerms);
		boolean newState = permission.hasPermission(rolePerms);

		database.updateRolePermissions(team, roleName, rolePerms);

		context.getSource().sendSuccess(() -> Component.literal("Updated " + permissionName + " for " + roleName + " OLD VALUE -> " + oldState + " NEW VALUE -> " + newState).withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int createRole(CommandContext<CommandSourceStack> context){
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		if(team == null){
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if(!Permission.MANAGE_ROLES.hasPermission(database.getPlayerPermission(player, team))){
			context.getSource().sendFailure(Component.literal("You do not have permission to create a role")
				.withStyle(ChatFormatting.RED));
			return 0;
		}
		String roleName = StringArgumentType.getString(context, "role_name");

		database.addRole(team, roleName, 0);
		context.getSource().sendSuccess(() -> Component.literal("Added Role: " + roleName + " to " + team.getName()).withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int teamInfo(CommandContext<CommandSourceStack> context){
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if(team == null){
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		//TODO: Make the info stuff look nice. I am to lazy to do so.
		Component chatMsg = Component.literal("Put Info Here");

		context.getSource().sendSuccess(() -> chatMsg, true);
		return 1;
	}

	private static int createTeam(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		if (database.getPlayerTeam(player) != null) {
			context.getSource().sendFailure(Component.literal("You are already in a team, please leave before creating a new one")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String name = StringArgumentType.getString(context, "name");
		String hex = StringArgumentType.getString(context, "color").replace("#", "");
		String tag = StringArgumentType.getString(context, "tag");

		String description;
		try {
			description = StringArgumentType.getString(context, "description");
		} catch (IllegalArgumentException e) {
			description = "";
		}

		Color color;
		try {
			color = new Color((int) Long.parseLong(hex, 16), true);
		} catch (NumberFormatException e) {
			context.getSource().sendFailure(Component.literal("Invalid hex color: #" + hex).withStyle(ChatFormatting.RED));
			return 0;
		}

		Team team = Team.builder().name(name).id(UUID.randomUUID()).color(color).description(description).tag(tag).build();
		database.addTeam(team);
		TeamRole ownerRole = database.getRoleByName(team, TeamRole.OWNER_ROLE_NAME);
		database.addPlayerToTeam(player, team, ownerRole);
		context.getSource().sendSuccess(() -> Component.literal("Team Created!").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int promptDeleteTeam(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_TEAM.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to delete this team")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		context.getSource().sendSuccess(() ->
			Component.literal("Are you sure you would like to delete ")
				.withStyle(ChatFormatting.RED)
				.append(Component.literal(team.getName())
					.withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED))
				.append(Component.literal("? "))
				.append(Component.literal("[YES]")
					.withStyle(style -> style
						.withColor(ChatFormatting.GREEN)
						.withBold(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitol team confirm_disband"))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to confirm deletion")))))
				.append(Component.literal(" "))
				.append(Component.literal("[NO]")
					.withStyle(style -> style
						.withColor(ChatFormatting.DARK_RED)
						.withBold(true))), false);
		return 1;
	}

	private static int confirmDeleteTeam(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_TEAM.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to delete this team")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		List<ClaimedChunk> chunks = database.getTeamChunks(team);
		database.removeTeam(team);

		for (ClaimedChunk chunk : chunks) {
			ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(chunk.dimension()));
			ServerLevel level = context.getSource().getServer().getLevel(dimKey);
			if (level == null) continue;
			ChunkPos chunkPos = new ChunkPos(chunk.chunkX(), chunk.chunkZ());
			S2CChunkRemove packet = new S2CChunkRemove(new Vector3f(chunkPos.x, 0, chunkPos.z));
			PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, packet);
		}

		context.getSource().sendSuccess(() -> Component.literal("Team deleted.").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int kickPlayer(CommandContext<CommandSourceStack> context) {

		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		String playerName = StringArgumentType.getString(context, "player");
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.KICK_MEMBERS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to kick players from this team")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
		Optional<GameProfile> profile = profileCache.get(playerName);
		if(profile.isEmpty()){
			context.getSource().sendFailure(Component.literal("There is no player called " + profile.get().getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfile gameProfile = profile.get();

		database.removePlayerFromTeam(gameProfile.getId(), team);

		context.getSource().sendSuccess(() -> Component.literal("Kicked " + gameProfile.getName() + " from the team")
			.withStyle(ChatFormatting.GREEN), true);

		return 1;
	}
}
