package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.server.commands.chunks.ClaimChunk;
import com.createcivilization.capitol.server.commands.debug.GetChunks;
import com.createcivilization.capitol.server.commands.debug.GetTeams;
import com.createcivilization.capitol.server.commands.teams.CreateTeam;
import com.createcivilization.capitol.server.commands.teams.DisbandTeam;
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
		new DisbandTeam().register(dispatcher);

		// Chunks
		new ClaimChunk().register(dispatcher);

		// Debug
		new GetTeams().register(dispatcher);
		new GetChunks().register(dispatcher);
	}
}