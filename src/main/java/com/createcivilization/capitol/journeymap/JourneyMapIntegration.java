package com.createcivilization.capitol.journeymap;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import journeymap.client.api.*;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.*;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.PolygonHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import org.jetbrains.annotations.Nullable;

import java.util.*;

@ClientPlugin
@SuppressWarnings("NullableProblems")
public class JourneyMapIntegration implements IClientPlugin {

	@SuppressWarnings("FieldCanBeLocal")
	private IClientAPI api;

	@Override
	public void initialize(IClientAPI iClientAPI) {
		System.out.println("Capitol initializing JourneyMap integration...");
		this.api = iClientAPI;
		this.api.subscribe(this.getModId(), EnumSet.of(ClientEvent.Type.DISPLAY_UPDATE));
	}

	@Override
	public String getModId() {
		return "capitol";
	}

	private final Map<String, PolygonOverlay> overlays = new HashMap<>(); // teamId : overlay

	@Override
	public void onEvent(ClientEvent clientEvent) {
		if (clientEvent.type != ClientEvent.Type.DISPLAY_UPDATE) return; // Ignore if not the event we want
		if (!ClientConstants.chunksDirty) return; // Only update chunks when they've been changed (claimed or unclaimed)
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
						if (prevOverlay != null) api.remove(prevOverlay); // Remove old overlay if it was present
						api.show(overlay); // Show current overlay
						overlays.put(teamId, overlay); // Store current overlay
					} catch (Exception e) {
						throw new RuntimeException("Failed to render claims!", e);
					}
				}
			}
		}
		ClientConstants.chunksDirty = false; // We've updated all chunks, no need to update again
	}
}