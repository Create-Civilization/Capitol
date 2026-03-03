package com.createcivilization.capitol.old.old.event;

import com.createcivilization.capitol.*;
import com.createcivilization.capitol.old.old.KeyBindings;
import com.createcivilization.capitol.old.old.constants.ClientConstants;
import com.createcivilization.capitol.old.old.gui.screen.BookMenu;
import com.createcivilization.capitol.old.old.gui.screen.CreateTeamScreen;
import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.old.payloads.toserver.requests.C2SClaimCurrentChunk;
import com.createcivilization.capitol.old.old.payloads.toserver.requests.C2SSendTeamMessage;
import com.createcivilization.capitol.old.old.team.OldTeam;

import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.*;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

import java.util.*;

import static com.createcivilization.capitol.old.old.constants.ClientConstants.getPlayerTeam;
import static com.createcivilization.capitol.old.old.constants.ClientConstants.playerTeam;

@EventBusSubscriber(modid = Capitol.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientEvents {

	@SubscribeEvent
	public static void onLeave(ClientPlayerNetworkEvent.LoggingOut event) {
		DataManager.TeamData.LOADED_OLD_TEAMS.clear();
	}

	@SubscribeEvent
	public static void onChat(ClientChatEvent event) {
		if (!ClientConstants.teamChat) return;
		event.setCanceled(true);
		String message = event.getMessage();
		PacketHandler.sendToServer(new C2SSendTeamMessage(message));
	}

	@SubscribeEvent
	public static void clientTick(ClientTickEvent.Post event) {
		final LocalPlayer player = ClientConstants.INSTANCE.player;
		if (player == null) return;
		final long timeStamp = System.currentTimeMillis() / 1000L;

		if (KeyBindings.openMenu.consumeClick()) {
			ObjectHolder<OldTeam> playerTeam = getPlayerTeam();
			if (playerTeam.isPresent()) ClientConstants.INSTANCE.setScreen(new BookMenu());
			else ClientConstants.INSTANCE.setScreen(new CreateTeamScreen());
		}

		if (KeyBindings.viewChunks.consumeClick()) {
			ClientConstants.viewChunks = !ClientConstants.viewChunks;
			player.displayClientMessage(Component.literal("Now " + (ClientConstants.viewChunks ? "showing" : "hiding") + " claim borders"), true);
		}

		if (KeyBindings.toggleTeamChat.consumeClick()) {
			if (getTeamOrDisplayClientMessage(player).isPresent()) {
				ClientConstants.teamChat = !ClientConstants.teamChat;
				ClientConstants.INSTANCE.player.displayClientMessage(Component.literal("Now talking in " + (ClientConstants.teamChat ? "receivingOldTeam chat" : "public chat")), false);
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
				PacketHandler.sendToServer(new C2SClaimCurrentChunk(0));
			}
		}

		if (ClientConstants.viewChunks && timeStamp % 2 == 0) {
			Level clientLevel = ClientConstants.INSTANCE.level;
			if (clientLevel == null) return;
			ResourceLocation dimension = clientLevel.dimension().location();

			// Render claim borders
			for (OldTeam oldTeam : DataManager.TeamData.LOADED_OLD_TEAMS) {
				// Remove all non-loaded chunks from list
				List<ChunkPos> chunks = oldTeam.getDimensionalData(dimension).getAllChildChunks();
				if (chunks == null) continue;
				chunks = chunks.stream().filter(
					chunkPos -> clientLevel.hasChunk(chunkPos.x, chunkPos.z)
				).toList();
				for (ChunkPos chunkPos : chunks) {
					displayClaimBorderVertice(chunkPos, oldTeam, -1, 0, clientLevel, dimension, player);
					displayClaimBorderVertice(chunkPos, oldTeam, 0, -1, clientLevel, dimension, player);
					displayClaimBorderVertice(chunkPos, oldTeam, 1, 0, clientLevel, dimension, player);
					displayClaimBorderVertice(chunkPos, oldTeam, 0, 1, clientLevel, dimension, player);
				}
			}
		}
	}

	public static ObjectHolder<OldTeam> getTeamOrDisplayClientMessage(LocalPlayer player) {
		ObjectHolder<OldTeam> teamHolder = TeamUtils.getTeam(player);
		if (teamHolder.isEmpty()) {
			player.displayClientMessage(ClientConstants.NOT_IN_TEAM, true);
			return teamHolder;
		} else {
			playerTeam.setFrom(teamHolder);
			return playerTeam;
		}
	}

	private static void displayClaimBorderVertice(ChunkPos chunkPos, OldTeam oldTeam, int xDiff, int zDiff, Level level, ResourceLocation dimension, LocalPlayer player) {
		// Avoid displaying vertices on which another chunk is at
		if (chunkIsOfTheSameTeam(oldTeam, new ChunkPos(chunkPos.x - xDiff, chunkPos.z - zDiff), dimension)) return;
		for (int i = -8; i < 8; i++) {
			// North edge (Z constant, X varies)
			level.addParticle(
				ParticleTypes.HAPPY_VILLAGER,
				chunkPos.getMiddleBlockX() - (xDiff * 8) + (zDiff * i), player.position().y + 1, chunkPos.getMiddleBlockZ() - (zDiff * 8) + (xDiff * i),
				0.0, 0.0, 0.0
			);
		}
	}

	private static boolean chunkIsOfTheSameTeam(OldTeam baseOldTeam, ChunkPos chunkToCheck, ResourceLocation dimension) {
		ObjectHolder<OldTeam> holder = TeamUtils.getTeam(chunkToCheck, dimension);
		if (holder.isEmpty()) return false;
		return Objects.equals(baseOldTeam.getTeamId(), holder.getOrThrow().getTeamId());
	}
}