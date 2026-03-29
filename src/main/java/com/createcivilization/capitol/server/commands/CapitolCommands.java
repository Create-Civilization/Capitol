package com.createcivilization.capitol.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CapitolCommands {
	public static void init(IEventBus bus) {
		NeoForge.EVENT_BUS.addListener(CapitolCommands::register);
	}

	private static void register(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		registerCommand(dispatcher, ClaimCommand.register());
		registerCommand(dispatcher, TeamCommand.register());
	}

	private static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher, LiteralArgumentBuilder<CommandSourceStack> command){
		dispatcher.register(Commands.literal("capitol").then(command));
	}
}
