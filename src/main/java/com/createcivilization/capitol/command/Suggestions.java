package com.createcivilization.capitol.command;

import com.createcivilization.capitol.constants.CommonConstants;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;

import java.awt.Color;
import java.util.Arrays;

public class Suggestions {

	public static final SuggestionProvider<CommandSourceStack> TEAM_NAMES = (context, builder) -> {
		TeamUtils.loadedTeams.stream().map(Team::getQuotedName).forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> TEAM_IDS = (context, builder) -> {
		TeamUtils.loadedTeams.stream().map(Team::getTeamId).forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> ROLES = (context, builder) -> {
		var player = context.getSource().getPlayer();
		assert player != null;
		Team team = TeamUtils.getTeam(player).getOrThrow();
		String playerRole = team.getPlayerRole(player.getUUID());
		Arrays.stream(team.getRoles())
			.filter(role -> !TeamUtils.isRoleHigher(team, playerRole, role))
			.forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> PERMISSIONS = (context, builder) -> {
		PermissionUtil.permissions.forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> COLORS = (context, builder) -> {
		CommonConstants.Colors.colors.keySet().forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> COLORS_RGB = (context, builder) -> {
		CommonConstants.Colors.colors.values().stream().map(Color::getRGB).forEach(builder::suggest);
		return builder.buildFuture();
	};
}