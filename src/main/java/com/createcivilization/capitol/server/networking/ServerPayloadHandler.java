package com.createcivilization.capitol.server.networking;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.C2SChunkRequest;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.joml.Vector3f;

public class ServerPayloadHandler {

	public static void handleChunkRequest(final C2SChunkRequest request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		ChunkPos chunkPos = new ChunkPos((int) request.chunkCords().x, (int) request.chunkCords().z);
		Team team = database.getChunkOwner(chunkPos, context.player().level());
		if(team == null) return;
		S2CChunkData packet = new S2CChunkData(request.chunkCords(), team);
		PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) context.player().level(), chunkPos, packet);
	}

}
