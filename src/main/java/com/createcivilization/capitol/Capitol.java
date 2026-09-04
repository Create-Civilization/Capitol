package com.createcivilization.capitol;

import com.createcivilization.capitol.common.CapitolCreativeTab;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.ClaimedChunk;
import com.createcivilization.capitol.common.item.CapitolItems;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.managers.ProtectionManager;
import com.createcivilization.capitol.server.commands.CapitolCommands;
import com.createcivilization.capitol.common.networking.CapitolNetworking;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

import net.neoforged.api.distmarker.Dist;
import com.createcivilization.capitol.client.renderer.BorderRenderer;
import com.createcivilization.capitol.client.renderer.BorderWallRenderer;
import com.createcivilization.capitol.client.renderer.SubClaimBorderRenderer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.List;

@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";
	public static final Logger LOGGER = LogUtils.getLogger();

    public Capitol(IEventBus modEventBus, ModContainer container) {
		container.registerConfig(ModConfig.Type.SERVER, CapitolConfig.SPEC);

		CapitolItems.register(modEventBus);
		CapitolCreativeTab.register(modEventBus);

		Capitol.LOGGER.info("Registering Listeners.");

		NeoForge.EVENT_BUS.addListener(this::onServerStart);
		NeoForge.EVENT_BUS.addListener(this::onServerStarted);
		NeoForge.EVENT_BUS.addListener(this::onServerStop);

		LOGGER.info("Listeners successfully registered");

		if (FMLEnvironment.dist == Dist.CLIENT) {
			NeoForge.EVENT_BUS.register(BorderRenderer.class);
			NeoForge.EVENT_BUS.register(BorderWallRenderer.class);
			NeoForge.EVENT_BUS.register(SubClaimBorderRenderer.class);
		}

		CapitolCommands.init(NeoForge.EVENT_BUS);
		CapitolNetworking.init(modEventBus);
	}


	private void onServerStart(ServerStartingEvent event) {
		Path worldPath = event.getServer()
			.getWorldPath(LevelResource.ROOT);
		Capitol.LOGGER.info("Starting server, world path: {}", worldPath);
		DatabaseManager.init(worldPath);
		ProtectionManager.reload();
	}

	private void onServerStarted(ServerStartedEvent event) {
		if (!CapitolConfig.FORCELOAD_ENABLED.get()) return;

		List<ClaimedChunk> forceloaded = DatabaseManager.database.getAllForceloadedChunks();
		for (ClaimedChunk chunk : forceloaded) {
			ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(chunk.dimension()));
			ServerLevel level = event.getServer().getLevel(dimKey);
			if (level == null) continue;
			level.setChunkForced(chunk.chunkX(), chunk.chunkZ(), true);
		}
	}

	private void onServerStop(ServerStoppingEvent event) {
		DatabaseManager.closeConnection();
	}

}
