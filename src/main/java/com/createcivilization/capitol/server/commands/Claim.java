package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class Claim {

	//TODO REMOVE THIS AS THIS IS TEMPORARY

	static LiteralArgumentBuilder<CommandSourceStack> register(){
		return Commands.literal("claim")
			.then(Commands.literal("chunk")
				.executes(Claim::claim))
			.then(Commands.literal("info")
				.executes(Claim::info));
	}

	private static int claim(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CapitolDatabase database = DatabaseManager.database;
		ServerPlayer serverPlayer = context.getSource().getPlayer();
		if(serverPlayer == null){
			return 0;
		}
		Team playerTeam = database.getPlayerTeam(Objects.requireNonNull(context.getSource().getPlayer()));
		if (playerTeam == null) {
			return 0;
		}
		database.claimChunk(playerTeam, serverPlayer.chunkPosition(), serverPlayer.level());


		return 1;
	}

	private static int info(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CapitolDatabase database = DatabaseManager.database;
		Team playerTeam = database.getChunkOwner(context.getSource().getPlayer().chunkPosition(), context.getSource().getLevel());
		if (playerTeam == null) {
			context.getSource().getPlayer().sendSystemMessage(Component.literal("No Claim In This Chunk").withStyle(ChatFormatting.GREEN));
			return 1;
		}
		String temp = "Claimed By: " + playerTeam.getName();
		context.getSource().getPlayer().sendSystemMessage(Component.literal(temp).withStyle(ChatFormatting.GREEN));
		return 1;
	}

}
