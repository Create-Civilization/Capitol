package com.createcivilization.capitol;

import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.server.commands.CapitolCommands;
import com.createcivilization.capitol.server.networking.packets.BoarderPacket;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.awt.*;
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

	private void onChunkLoad(ChunkEvent.Load event) {
		if(event.getLevel().isClientSide()) return;
		ChunkPos chunkPos = event.getChunk().getPos();
		ResourceKey<Level> dimension = event.getChunk().getLevel().dimension();
		ServerLevel level = event.getLevel().getServer().getLevel(dimension);
		BoarderPacket packet = new BoarderPacket(chunkPos.toString());

		PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, packet);
	}
}