package com.createcivilization.capitol.event;

import com.createcivilization.capitol.*;
import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.packets.toserver.*;
import com.createcivilization.capitol.screen.*;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

import java.util.*;

import static com.createcivilization.capitol.constants.ClientConstants.playerTeam;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public static void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
		TeamUtils.loadedTeams.clear();
	}

	@SubscribeEvent
	public static void onChat(ClientChatEvent event) {
		if (!ClientConstants.teamChat) return;
		event.setCanceled(true);
		String message = event.getMessage();
		PacketHandler.sendToServer(new C2SSendTeamMessage(message));
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		final LocalPlayer player = ClientConstants.INSTANCE.player;
		if (player == null) return;
		final long timeStamp = System.currentTimeMillis() / 1000L;

		if (KeyBindings.openStatistics.consumeClick() && getTeamOrDisplayClientMessage(player).isPresent()) {
			ClientConstants.INSTANCE.setScreen(new TeamStatisticsScreen(playerTeam.getOrThrow()));
		}

		if (KeyBindings.viewChunks.consumeClick()) {
			ClientConstants.viewChunks = !ClientConstants.viewChunks;
			player.displayClientMessage(Component.literal("Now " + (ClientConstants.viewChunks ? "showing" : "hiding") + " claim borders"), true);
		}

		if (KeyBindings.openClaimMenu.consumeClick()) {
			if (getTeamOrDisplayClientMessage(player).isPresent()) ClientConstants.INSTANCE.setScreen(new TeamClaimManagerScreen());
			else ClientConstants.INSTANCE.setScreen(new CreateTeamScreen());
		}

		if (KeyBindings.toggleTeamChat.consumeClick()) {
			if (getTeamOrDisplayClientMessage(player).isPresent()) {
				ClientConstants.teamChat = !ClientConstants.teamChat;
				ClientConstants.INSTANCE.player.displayClientMessage(Component.literal("Now talking in " + (ClientConstants.teamChat ? "team chat" : "public chat")), false);
			}
		}

		if (KeyBindings.claim_chunk.consumeClick() && getTeamOrDisplayClientMessage(player).isPresent()) {
			if (!TeamUtils.chunkIsNearChildChunk(player.chunkPosition(), 1, player))
				player.displayClientMessage(
					ClientConstants.NOT_NEAR_CHUNK,
					true
				);
			else if (TeamUtils.isInClaimedChunk(player))
				player.displayClientMessage(
					ClientConstants.CHUNK_ALREADY_CLAIMED,
					true
				);
			else {
				player.displayClientMessage(
					ClientConstants.CHUNK_SUCCESSFULLY_CLAIMED,
					true
				);
				PacketHandler.sendToServer(new C2SClaimCurrentChunk());
			}
		}

		if (ClientConstants.viewChunks && timeStamp % 2 == 0) {
			Level clientLevel = ClientConstants.INSTANCE.level;
			if (clientLevel == null) return;
			ResourceLocation dimension = clientLevel.dimension().location();

			// Render claim borders
			for (Team team : TeamUtils.loadedTeams) {
				// Remove all non-loaded chunks from list
				List<ChunkPos> chunks = team.getDimensionalData(dimension).getAllChildChunks();
				if (chunks == null) continue;
				chunks = chunks.stream().filter(
					chunkPos -> clientLevel.hasChunk(chunkPos.x, chunkPos.z)
				).toList();
				for (ChunkPos chunkPos : chunks) {
					displayClaimBorderVertice(chunkPos, team, -1, 0, clientLevel, dimension, player);
					displayClaimBorderVertice(chunkPos, team, 0, -1, clientLevel, dimension, player);
					displayClaimBorderVertice(chunkPos, team, 1, 0, clientLevel, dimension, player);
					displayClaimBorderVertice(chunkPos, team, 0, 1, clientLevel, dimension, player);
				}
			}
		}
	}

	public static ObjectHolder<Team> getTeamOrDisplayClientMessage(LocalPlayer player) {
		ObjectHolder<Team> teamHolder = TeamUtils.getTeam(player);
		if (teamHolder.isEmpty()) {
			player.displayClientMessage(ClientConstants.NOT_IN_TEAM, true);
			return teamHolder;
		} else {
			playerTeam.setFrom(teamHolder);
			return playerTeam;
		}
	}

	private static void displayClaimBorderVertice(ChunkPos chunkPos, Team team, int xDiff, int zDiff, Level level, ResourceLocation dimension, LocalPlayer player) {
		// Avoid displaying vertices on which another chunk is at
		if (chunkIsOfTheSameTeam(team, new ChunkPos(chunkPos.x - xDiff, chunkPos.z - zDiff), dimension)) return;
		for (int i = -8; i < 8; i++) {
			// North edge (Z constant, X varies)
			level.addParticle(
				ParticleTypes.HAPPY_VILLAGER,
				chunkPos.getMiddleBlockX() - (xDiff * 8) + (zDiff * i), player.position().y + 1, chunkPos.getMiddleBlockZ() - (zDiff * 8) + (xDiff * i),
				0.0, 0.0, 0.0
			);
		}
	}

	private static boolean chunkIsOfTheSameTeam(Team baseTeam, ChunkPos chunkToCheck, ResourceLocation dimension) {
		ObjectHolder<Team> holder = TeamUtils.getTeam(chunkToCheck, dimension);
		if (holder.isEmpty()) return false;
		return Objects.equals(baseTeam.getTeamId(), holder.getOrThrow().getTeamId());
	}
}