package com.createcivilization.capitol;

import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.server.commands.CapitolCommands;
import com.mojang.logging.LogUtils;

import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";
	public static final Logger LOGGER = LogUtils.getLogger();

    public Capitol(IEventBus modEventBus, ModContainer container) {
		NeoForge.EVENT_BUS.addListener(this::onServerStart);
		NeoForge.EVENT_BUS.addListener(this::onServerStop);

		CapitolCommands.init(NeoForge.EVENT_BUS);
    }


	private void onServerStart(ServerStartingEvent event) {
		Path worldPath = event.getServer()
			.getWorldPath(LevelResource.ROOT);
		DatabaseManager.init(worldPath);
	}

	private void onServerStop(ServerStoppingEvent event) {
		DatabaseManager.closeConnection();
	}
}