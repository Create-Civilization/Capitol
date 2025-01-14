package com.createcivilization.capitol.journeymap;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.event.ClientEvents;
import com.createcivilization.capitol.packets.toserver.C2SClaimChunk;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import journeymap.client.api.*;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.*;
import journeymap.client.api.event.forge.PopupMenuEvent;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.PolygonHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.*;

import java.util.*;

@ClientPlugin
public class JourneyMapIntegration implements IClientPlugin {

	private IClientAPI api;

	@Override
	public void initialize(@NotNull IClientAPI iClientAPI) {
		System.out.println("Capitol initializing JourneyMap integration...");
		this.api = iClientAPI;
		this.api.subscribe(this.getModId(), EnumSet.of(ClientEvent.Type.DISPLAY_UPDATE));
		MinecraftForge.EVENT_BUS.addListener(this::onPopupMenuEvent);
	}

	@Override
	public String getModId() {
		return "capitol";
	}

	public void onPopupMenuEvent(PopupMenuEvent popupMenuEvent) {
		var menu = popupMenuEvent.getPopupMenu();

		var player = ClientConstants.INSTANCE.player;
		assert player != null;
		menu.addMenuItem(Component.translatable("gui.journeymap.capitol.claim_chunk").getString(), (pos) -> {
			if (ClientEvents.getTeamOrDisplayClientMessage(player).isEmpty()) return;
			if (!TeamUtils.nearClaimedChunk(new ChunkPos(pos), 1, player))
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
				PacketHandler.sendToServer(new C2SClaimChunk(pos));
			}
		});

		menu.addMenuItem(Component.translatable("gui.journeymap.capitol.unclaim_chunk").getString(), (pos) -> {

		});
	}

	private final Map<String, PolygonOverlay> overlays = new HashMap<>();

	@Override
	public void onEvent(ClientEvent clientEvent) {
		if (clientEvent.type != ClientEvent.Type.DISPLAY_UPDATE) return;
		if (!ClientConstants.chunksDirty) return;
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
						new ShapeProperties().setFillColor(team.getColor().getRGB()),
						poly
					);
					try {
						if (prevOverlay != null) api.remove(prevOverlay);
						api.show(overlay);
						overlays.put(teamId, overlay);
					} catch (Exception e) {
						throw new RuntimeException("Failed to render claims!", e);
					}
				}
			}
		}
		ClientConstants.chunksDirty = false;
	}
}