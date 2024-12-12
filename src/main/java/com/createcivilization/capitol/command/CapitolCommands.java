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
        new CreateTeamCommand().register(event.getDispatcher());
		new ReloadTeamsFromFileCommand().register(event.getDispatcher());
		new ReloadTeamsCommand().register(event.getDispatcher());
    }
}