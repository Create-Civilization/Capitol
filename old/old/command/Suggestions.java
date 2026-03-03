package com.createcivilization.capitol.old.old.command;

import com.createcivilization.capitol.old.old.constants.CommonConstants;
import com.createcivilization.capitol.old.old.team.OldTeam;

import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.common.utils.PermissionUtil;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;

import java.awt.Color;
import java.util.Arrays;

public class Suggestions {

	public static final SuggestionProvider<CommandSourceStack> TEAM_NAMES = (context, builder) -> {
		DataManager.TeamData.LOADED_OLD_TEAMS.stream().map(OldTeam::getQuotedName).forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> TEAM_IDS = (context, builder) -> {
		DataManager.TeamData.LOADED_OLD_TEAMS.stream().map(OldTeam::getTeamId).forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> ROLES = (context, builder) -> {
		var player = context.getSource().getPlayer();
		assert player != null;
		OldTeam oldTeam = TeamUtils.getTeam(player).getOrThrow();
		String playerRole = oldTeam.getRole(player.getUUID());
		Arrays.stream(oldTeam.getRoles())
			.filter(role -> !TeamUtils.isRoleHigher(oldTeam, playerRole, role))
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
		CommonConstants.Colors.getColorsStream().map(Color::getRGB).forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> COLORS_RED = (context, builder) -> {
		CommonConstants.Colors.getColorsStream().map(Color::getRed).forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> COLORS_GREEN = (context, builder) -> {
		CommonConstants.Colors.getColorsStream().map(Color::getGreen).forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> COLORS_BLUE = (context, builder) -> {
		CommonConstants.Colors.getColorsStream().map(Color::getBlue).forEach(builder::suggest);
		return builder.buildFuture();
	};

	public static final SuggestionProvider<CommandSourceStack> COLORS_HEX = (context, builder) -> {
		CommonConstants.Colors.getColorsStream().map(CommonConstants.Colors::getHex).forEach(builder::suggest);
		return builder.buildFuture();
	};
}