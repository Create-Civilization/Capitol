package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.toclient.syncing.S2CAddPlayerName;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.PacketHandler;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wiiu.mavity.util.ObjectHolder;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEvents {

	public static MinecraftServer server;
	private static final String LASTCHUNKKEY = Capitol.MOD_ID + ".last_chunk";

	@SubscribeEvent
	public static void onServerStarted(ServerStartedEvent event) {
		server = event.getServer();
	}

	@SubscribeEvent
	public static void onPlayerEnterChunk(EntityEvent.EnteringSection event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		ObjectHolder<Team> holder = TeamUtils.getTeam(player.chunkPosition(), player.level().dimension().location());
		String teamId;
		String claimName;
		Team team;
		if (holder.isEmpty()) {
			teamId = "";
			claimName = "the wild";
		}else{
			team = holder.getOrThrow();
			teamId = team.getTeamId();
			claimName = team.getName();
		}
		if (Objects.equals(teamId, player.getPersistentData().getString(LASTCHUNKKEY))) return;
		player.displayClientMessage(Component.literal("Now entering " + claimName), true);
		player.getPersistentData().putString(LASTCHUNKKEY, claimName);
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
		if (playerLoggedInEvent.getEntity() instanceof ServerPlayer player) {
			player.getPersistentData().putString(LASTCHUNKKEY, "");
			TeamUtils.synchronizeServerDataWithPlayer(player);
			for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) if (serverPlayer != player) PacketHandler.sendToPlayer(new S2CAddPlayerName(serverPlayer), player);
			PacketHandler.sendToAllClients(new S2CAddPlayerName(player));
		}
	}
}