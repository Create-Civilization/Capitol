package com.createcivilization.capitol;

import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.server.commands.CapitolCommands;
import com.createcivilization.capitol.server.networking.CapitolNetworking;
import com.createcivilization.capitol.server.networking.packets.BorderPacket;
import com.createcivilization.capitol.server.networking.packets.BorderRemovePacket;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import com.createcivilization.capitol.client.renderer.BorderRenderer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";
	public static final Logger LOGGER = LogUtils.getLogger();

    public Capitol(IEventBus modEventBus, ModContainer container) {
		NeoForge.EVENT_BUS.addListener(this::onServerStart);
		NeoForge.EVENT_BUS.addListener(this::onServerStop);

		NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
		if (FMLEnvironment.dist == Dist.CLIENT) {
			NeoForge.EVENT_BUS.addListener(this::onClientDisconnect);
			NeoForge.EVENT_BUS.register(BorderRenderer.class);
		}

		CapitolCommands.init(NeoForge.EVENT_BUS);
		CapitolNetworking.init(modEventBus);
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
		CapitolDatabase database = DatabaseManager.database;
		if(database.getConnection() == null) return;
		if(event.getLevel().isClientSide()) return;
		ChunkPos chunkPos = event.getChunk().getPos();
		ResourceKey<Level> dimension = event.getChunk().getLevel().dimension();
		ServerLevel level = event.getLevel().getServer().getLevel(dimension);
		Team team = database.getChunkOwner(chunkPos, level);
		if(team == null) {
			BorderRemovePacket packet = new BorderRemovePacket(new Vector3f(chunkPos.x, 0, chunkPos.z));
			PacketDistributor.sendToPlayersTrackingChunk(level,chunkPos,packet);
			return;
		}
		BorderPacket packet = new BorderPacket(new Vector3f(chunkPos.x, 0, chunkPos.z), team);
		PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, packet);
	}

	private void onClientDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
		ClientClaimCache.clearClaims();
	}


}