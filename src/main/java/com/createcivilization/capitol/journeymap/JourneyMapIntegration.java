package com.createcivilization.capitol.journeymap;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import journeymap.client.api.*;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.PolygonHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

// See MapPolygon.class
// TODO: Fix this lol
@ClientPlugin
@SuppressWarnings("NullableProblems")
public class JourneyMapIntegration implements IClientPlugin {

	@SuppressWarnings("FieldCanBeLocal")
	private IClientAPI api;

	@Override
	public void initialize(IClientAPI iClientAPI) {
		System.out.println("Capitol initializing JourneyMap integration...");
		this.api = iClientAPI;
		MinecraftForge.EVENT_BUS.addListener(this::updateChunks);
	}

	@Override
	public String getModId() {
		return "capitol";
	}

	@Override
	public void onEvent(ClientEvent clientEvent) {}

	public void updateChunks(final TickEvent.LevelTickEvent tick) {
		if (tick.side == LogicalSide.CLIENT) {
			for (Team team : TeamUtils.loadedTeams) {
				for (var claimedChunks : team.getClaimedChunks().entrySet()) {
					var player = Minecraft.getInstance().player;
					assert player != null;
					var polygon = PolygonHelper.createChunksPolygon(claimedChunks.getValue(), player.getBlockY());
					for (var poly : polygon) {
						PolygonOverlay overlay = new PolygonOverlay(
							"captiol",
							team.getTeamId(),
							ResourceKey.create(Registries.DIMENSION, claimedChunks.getKey()),
							new ShapeProperties().setFillColor(0xA020F0), // Purple to test
							poly
						);
						try {
							api.remove(overlay);
							api.show(overlay);
						} catch (Exception e) {
							throw new RuntimeException("Failed to render claims!", e);
						}
					}
				}
			}
		}
	}
}