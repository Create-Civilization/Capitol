package com.createcivilization.capitol.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CapitolCommands {
	public static void init(IEventBus bus) {
		NeoForge.EVENT_BUS.addListener(CapitolCommands::register);
	}

	private static void register(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(Claim.register());
		dispatcher.register(TeamCommand.register());
	}
}
