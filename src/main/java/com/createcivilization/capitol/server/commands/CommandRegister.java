package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.server.commands.debug.GetTeams;
import com.createcivilization.capitol.server.commands.teams.CreateTeam;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Capitol.MOD_ID)
public class CommandRegister {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

		// Teams
		new CreateTeam().register(dispatcher);

		// Debug
		new GetTeams().register(dispatcher);
	}
}