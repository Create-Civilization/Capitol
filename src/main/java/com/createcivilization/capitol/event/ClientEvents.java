package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.KeyBindings;
import com.createcivilization.capitol.packets.toserver.C2SClaimChunk;
import com.createcivilization.capitol.screen.CreateTeamScreen;
import com.createcivilization.capitol.screen.TeamClaimManagerScreen;
import com.createcivilization.capitol.screen.TeamStatisticsScreen;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.PacketHandler;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wiiu.mavity.util.ObjectHolder;

import java.util.*;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

	private static final Component NOT_IN_TEAM = Component.literal("You are not in a team");
	private static final Component NOT_NEAR_CHUNK = Component.literal("You must be next to a claimed chunk to do this");
	private static final Component CLAIMED_CHUNK = Component.literal("Chunk already claimed");
	private static final Component SUCCESS_CHUNK = Component.literal("Chunk successfully claimed");

	private static final Minecraft instance = Minecraft.getInstance();
	private static boolean viewChunks;
	public static Map<UUID, String> playerMap = new HashMap<>();
	private static ObjectHolder<Team> playerTeam;

	@SubscribeEvent
	public static void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
		TeamUtils.loadedTeams.clear();
		ClientEvents.playerMap.clear();
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		final LocalPlayer player = instance.player;
		if (player == null) return;
		final long timeStamp = System.currentTimeMillis() / 100L;

		if (
			KeyBindings.openStatistics.consumeClick()
			&& getTeamOrDisplayClientMessage(player).isPresent()
		) {
			instance.setScreen(new TeamStatisticsScreen(playerTeam.getOrThrow()));
		}
		if (KeyBindings.viewChunks.consumeClick()) {
			viewChunks = !viewChunks;
			player.displayClientMessage(Component.literal("Now " + (viewChunks ? "showing" : "hiding") + " claim borders"), true);
		}

		if (
			KeyBindings.openClaimMenu.consumeClick()
		) {
			if(getTeamOrDisplayClientMessage(player).isPresent())
				instance.setScreen(new TeamClaimManagerScreen(playerTeam.getOrThrow()));
			else
				instance.setScreen(new CreateTeamScreen());
		}

		if (
			KeyBindings.claim_chunk.consumeClick()
			&& getTeamOrDisplayClientMessage(player).isPresent()
		) {
			if (!TeamUtils.nearClaimedChunk(player.chunkPosition(), 1, player))
				player.displayClientMessage(
					NOT_NEAR_CHUNK,
					true
					);
			else if (TeamUtils.isInClaimedChunk(player))
				player.displayClientMessage(
					CLAIMED_CHUNK,
					true
				);
			else {
				player.displayClientMessage(
					SUCCESS_CHUNK,
					true
				);
				PacketHandler.sendToServer(new C2SClaimChunk());
			}
		}

		if (viewChunks && timeStamp % 5 == 0) {
			Level clientLevel = instance.level;
			if (clientLevel == null) return;
			ResourceLocation dimension = clientLevel.dimension().location();

			// Render claim borders
			for (Team team : TeamUtils.loadedTeams) {
				// Remove all non-loaded chunks from list
				List<ChunkPos> chunks = team.getClaimedChunksOfDimension(dimension);
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

	private static ObjectHolder<Team> getTeamOrDisplayClientMessage(LocalPlayer player) {
		ObjectHolder<Team> teamHolder = TeamUtils.getTeam(player);
		if (teamHolder.isEmpty()) {
			player.displayClientMessage(NOT_IN_TEAM, true);
			return teamHolder;
		}else {
			playerTeam = teamHolder;
			return playerTeam;
		}
	}

	private static void displayClaimBorderVertice (ChunkPos chunkPos, Team team, int xDiff, int zDiff, Level level, ResourceLocation dimension, LocalPlayer player) {
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

	private static boolean chunkIsOfTheSameTeam (Team baseTeam, ChunkPos chunkToCheck, ResourceLocation dimension) {
		ObjectHolder<Team> holder = TeamUtils.getTeam(chunkToCheck, dimension);
		if (holder.isEmpty()) return false;
		return Objects.equals(baseTeam.getTeamId(), holder.getOrThrow().getTeamId());
	}
}
