package com.createcivilization.capitol.old.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.team.Team;

import com.createcivilization.capitol.old.util.data.DataManager;
import com.createcivilization.capitol.old.util.team.TeamUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEvents {

	private static final String LAST_CHUNK_TEAM_ID = Capitol.MOD_ID + ".last_chunk";

	@SubscribeEvent
	@SuppressWarnings("resource")
	public static void onPlayerEnterChunk(EntityEvent.EnteringSection event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		ObjectHolder<Team> holder = TeamUtils.getTeam(player.chunkPosition(), player.level().dimension().location());
		String teamId;
		String claimName;
		if (holder.isEmpty()) {
			teamId = "";
			claimName = "the wild";
		} else {
			Team team = holder.getOrThrow();
			teamId = team.getTeamId();
			claimName = team.getName();
		}
		CompoundTag data = player.getPersistentData();
		if (!teamId.equals(data.getString(LAST_CHUNK_TEAM_ID))) player.displayClientMessage(Component.literal("Now entering " + claimName), true);
		data.putString(LAST_CHUNK_TEAM_ID, teamId);
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
		if (playerLoggedInEvent.getEntity() instanceof ServerPlayer player) {
			player.getPersistentData().putString(LAST_CHUNK_TEAM_ID, "");
			DataManager.synchronizeServerDataWithPlayer(player);
		}
	}
}