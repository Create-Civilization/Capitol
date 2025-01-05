package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.toclient.S2CaddChunk;
import com.createcivilization.capitol.packets.toclient.S2CaddTeam;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.PacketHandler;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEvents {

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
		if (!(playerLoggedInEvent.getEntity() instanceof ServerPlayer player)) return;
		for (Team team : TeamUtils.loadedTeams) {
			PacketHandler.sendToPlayer(new S2CaddTeam(team), player);
			for (Map.Entry<ResourceLocation, List<ChunkPos>> chunkEntry : team.getClaimedChunks().entrySet()) {
				for (ChunkPos chunkPos : chunkEntry.getValue()) {
					PacketHandler.sendToPlayer(new S2CaddChunk(team.getTeamId(), chunkPos, chunkEntry.getKey()), player);
				}
			}
		}
	}
}
