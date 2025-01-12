package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.toclient.syncing.S2CAddPlayerName;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import wiiu.mavity.util.ObjectHolder;

import java.awt.*;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEvents {

	private static final String LAST_CHUNK_TEAM_ID = Capitol.MOD_ID + ".last_chunk";

	@SubscribeEvent
	@SuppressWarnings("resource")
	public static void onPlayerEnterChunk(EntityEvent.EnteringSection event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		ObjectHolder<Team> holder = TeamUtils.getTeam(player.chunkPosition(), player.level().dimension().location());
		String teamId;
		String claimName;
		int color = Color.GREEN.getRGB();
		if (holder.isEmpty()) {
			teamId = "";
			claimName = "the wild";
		} else {
			Team team = holder.getOrThrow();
			teamId = team.getTeamId();
			claimName = team.getName();
			color = team.getColor().getRGB();
		}
		String lastChunkTeamId = player.getPersistentData().getString(LAST_CHUNK_TEAM_ID);
		System.out.println("Color: " + color);
		System.out.println("Expected color rgb setup: " + TextColor.NAMED_COLORS.get("blue").getValue());
		if (!teamId.equals(lastChunkTeamId)) {
			player.displayClientMessage(
				Component.literal("Now entering ")
					.append(Component.literal(claimName).setStyle(Style.EMPTY.withColor(color).withBold(true))),
				true
			);
		}
		player.getPersistentData().putString(LAST_CHUNK_TEAM_ID, teamId);
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
		if (playerLoggedInEvent.getEntity() instanceof ServerPlayer player) {
			player.getPersistentData().putString(LAST_CHUNK_TEAM_ID, "");
			TeamUtils.synchronizeServerDataWithPlayer(player);
			for (ServerPlayer serverPlayer : Capitol.server.getOrThrow().getPlayerList().getPlayers()) if (serverPlayer != player) PacketHandler.sendToPlayer(new S2CAddPlayerName(serverPlayer), player);
			PacketHandler.sendToAllClients(new S2CAddPlayerName(player));
		}
	}
}