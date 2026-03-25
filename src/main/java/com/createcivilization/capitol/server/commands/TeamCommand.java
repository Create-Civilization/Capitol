package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.common.data.Role;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class TeamCommand {

	//TODO REMOVE THIS AS THIS IS TEMPORARY

	static LiteralArgumentBuilder<CommandSourceStack> register(){
		return Commands.literal("team")
			.then(Commands.literal("create")
				.then(Commands.argument("name", StringArgumentType.string())
					.executes(TeamCommand::createTeam)));
	}

	private static int createTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CapitolDatabase database = DatabaseManager.database;
		String name = StringArgumentType.getString(context, "name");
		Team team = Team.builder().name(name).id(UUID.randomUUID()).build();
		database.addTeam(team);
		database.addPlayerToTeam(context.getSource().getPlayer(), team, Role.OWNER);
		context.getSource().getPlayer().sendSystemMessage(Component.literal("Team created!").withStyle(ChatFormatting.GREEN));
		return 1;
	}

}
