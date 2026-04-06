package com.createcivilization.capitol;

import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.server.commands.CapitolCommands;
import com.createcivilization.capitol.common.networking.CapitolNetworking;

import com.createcivilization.capitol.server.events.CreateEvents;
import com.mojang.logging.LogUtils;

import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import com.createcivilization.capitol.client.renderer.BorderRenderer;
import com.createcivilization.capitol.client.renderer.BorderWallRenderer;
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

		if (FMLEnvironment.dist == Dist.CLIENT) {
			NeoForge.EVENT_BUS.register(BorderRenderer.class);
			NeoForge.EVENT_BUS.register(BorderWallRenderer.class);
		}

		CapitolCommands.init(NeoForge.EVENT_BUS);
		CapitolNetworking.init(modEventBus);

//		if (ModList.get().isLoaded("create")) {
//			NeoForge.EVENT_BUS.register(CreateEvents.class);
//		}
    }


	private void onServerStart(ServerStartingEvent event) {
		Path worldPath = event.getServer()
			.getWorldPath(LevelResource.ROOT);
		DatabaseManager.init(worldPath);
	}

	private void onServerStop(ServerStoppingEvent event) {
		DatabaseManager.closeConnection();
	}


	private void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientClaimCache.clearClaims();
	}


}