package com.createcivilization.capitol.command;

import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;

public class Suggestions {

	public static final SuggestionProvider<CommandSourceStack> TEAM_NAME = (context, builder) -> {
		TeamUtils.loadedTeams.stream().map((t) -> "\"" + t.getName() + "\"").forEach(builder::suggest);
		return builder.buildFuture();
	};
}