package com.createcivilization.capitol.command;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.command.custom.*;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID)
public class CapitolCommands {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
		var dispatcher = event.getDispatcher();
        new CreateTeamCommand().register(dispatcher);
		new ReloadTeamsFromFileCommand().register(dispatcher);
		new ReloadTeamsCommand().register(dispatcher);
		new ClaimCurrentChunkCommand().register(dispatcher);
    }
}