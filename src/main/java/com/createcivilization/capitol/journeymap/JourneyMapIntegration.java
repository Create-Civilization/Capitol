package com.createcivilization.capitol.journeymap;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.event.ClientEvents;
import com.createcivilization.capitol.packets.toserver.C2SClaimChunk;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import journeymap.client.api.*;
import journeymap.client.api.display.*;
import journeymap.client.api.event.*;
import journeymap.client.api.event.forge.PopupMenuEvent;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.PolygonHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;

import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

import org.jetbrains.annotations.*;

import org.lwjgl.glfw.GLFW;

import java.util.*;

@ClientPlugin
public class JourneyMapIntegration implements IClientPlugin {

	private IClientAPI api;

	@Override
	public void initialize(@NotNull IClientAPI iClientAPI) {
		System.out.println("Capitol initializing JourneyMap integration...");
		this.api = iClientAPI;
		this.api.subscribe(this.getModId(), EnumSet.of(ClientEvent.Type.MAP_CLICKED));
		MinecraftForge.EVENT_BUS.addListener(this::onPopupMenuEvent);
		MinecraftForge.EVENT_BUS.addListener(this::updateChunks);
		MinecraftForge.EVENT_BUS.addListener(this::onKey);
		MinecraftForge.EVENT_BUS.addListener(this::clearCache);
	}

	@Override
	public String getModId() {
		return "capitol";
	}

	// Turns out you can crash the game if you don't clear this data :/
	public void clearCache(ClientPlayerNetworkEvent.LoggingOut event) {
		this.overlays.values().forEach(this.api::remove);
		this.overlays.clear();
		this.removeLastClickOverlayIfPresent();
		ClientConstants.toResetChunksTeamIds.clear();
		ClientConstants.chunksDirty = false;
	}

	public void updateChunks(TickEvent.LevelTickEvent event) {
		if (event.side != LogicalSide.CLIENT) return;
		if (System.currentTimeMillis() / 1000f % 5f != 0 && !ClientConstants.chunksDirty) return;

		// Cleanup old overlays from chunks that are no longer claimed
		this.overlays.keySet().stream()
			.filter(ClientConstants.toResetChunksTeamIds::contains)
			.forEach((teamId) -> this.api.remove(this.overlays.get(teamId)));
		ClientConstants.toResetChunksTeamIds.clear();

		// Cleanup old overlays from deleted teams
		for (String teamId : overlays.keySet()) {
			if (TeamUtils.loadedTeams.stream().noneMatch(team -> team.getTeamId().equals(teamId))) {
				this.api.remove(this.overlays.get(teamId));
				this.overlays.remove(teamId);
			}
		}

		for (Team team : TeamUtils.loadedTeams) {
			for (var claimedChunks : team.getClaimedChunks().entrySet()) {
				var player = Minecraft.getInstance().player;
				assert player != null;
				var polygon = PolygonHelper.createChunksPolygon(claimedChunks.getValue(), player.getBlockY());
				for (var poly : polygon) {
					String teamId = team.getTeamId();
					@Nullable PolygonOverlay prevOverlay = overlays.get(teamId);
					PolygonOverlay overlay = new PolygonOverlay(
						this.getModId(),
						teamId,
						ResourceKey.create(Registries.DIMENSION, claimedChunks.getKey()),
						new ShapeProperties()
							.setFillColor(team.getColor().getRGB())
							.setFillOpacity(0.25f),
						poly
					);
					try {
						if (prevOverlay != null) this.api.remove(prevOverlay);
						this.api.show(overlay);
						this.overlays.put(teamId, overlay);
					} catch (Exception e) {
						throw new RuntimeException("Failed to render claims!", e);
					}
				}
			}
		}

		ClientConstants.chunksDirty = false;
	}

	public void onPopupMenuEvent(PopupMenuEvent event) {
		if (event.getLayer() != PopupMenuEvent.Layer.FULLSCREEN) return;

		ModPopupMenu menu = event.getPopupMenu();
		LocalPlayer player = ClientConstants.INSTANCE.player;
		assert player != null;
		menu.addMenuItem(Component.translatable("gui.journeymap.capitol.claim_chunk").getString(), (pos) -> {
			if (ClientEvents.getTeamOrDisplayClientMessage(player).isEmpty()) return;
			ChunkPos chunkPos = new ChunkPos(pos);
			if (!TeamUtils.nearClaimedChunk(chunkPos, 1, player))
				player.displayClientMessage(
					ClientConstants.NOT_NEAR_CHUNK,
					true
				);
			else if (TeamUtils.isInClaimedChunk(player, pos))
				player.displayClientMessage(
					ClientConstants.CHUNK_ALREADY_CLAIMED,
					true
				);
			else {
				player.displayClientMessage(
					ClientConstants.CHUNK_SUCCESSFULLY_CLAIMED,
					true
				);
				PacketHandler.sendToServer(new C2SClaimChunk(chunkPos));
			}
			this.removeLastClickOverlayIfPresent();
		});

		// TODO: Work on this.
		menu.addMenuItem(Component.translatable("gui.journeymap.capitol.unclaim_chunk").getString(), (pos) -> {
			this.removeLastClickOverlayIfPresent();
		});
	}

	public void removeLastClickOverlayIfPresent() {
		if (lastClickOverlay != null) {
			this.api.remove(lastClickOverlay);
			lastClickOverlay = null;
		}
	}

	private final Map<String, PolygonOverlay> overlays = new HashMap<>();

	@Override
	public void onEvent(ClientEvent clientEvent) {
		if (clientEvent.type == ClientEvent.Type.MAP_CLICKED) handleMapClicked((FullscreenMapEvent.ClickEvent.Post) clientEvent);
	}

	private PolygonOverlay lastClickOverlay;

	public void handleMapClicked(FullscreenMapEvent.ClickEvent.Post event) {
		if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			var pos = event.getLocation();
			var displaySelector = PolygonHelper.createChunkPolygonForWorldCoords(pos.getX(), pos.getY(), pos.getZ());
			try {
				PolygonOverlay clickOverlay = new PolygonOverlay(
					this.getModId(),
					"capitolMouseSelector",
					event.getLevel(),
					new ShapeProperties()
						.setFillColor(-8388480) // Purple
						.setFillOpacity(0.25f),
					displaySelector
				);
				this.api.show(clickOverlay);
				lastClickOverlay = clickOverlay;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			if (lastClickOverlay != null) {
				this.api.remove(lastClickOverlay);
				lastClickOverlay = null;
			}
		}
	}

	public void onKey(InputEvent.Key event) {
		this.removeLastClickOverlayIfPresent();
	}
}