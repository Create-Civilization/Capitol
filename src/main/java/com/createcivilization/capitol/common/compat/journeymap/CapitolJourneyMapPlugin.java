package com.createcivilization.capitol.common.compat.journeymap;


import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.compat.journeymap.util.PolygonHelper;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.display.DisplayType;
import journeymap.api.v2.client.display.PolygonOverlay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

@SuppressWarnings("removal")
@JourneyMapPlugin(apiVersion = "v2")
public class CapitolJourneyMapPlugin implements IClientPlugin {

	private static final int REFRESH_INTERVAL_TICKS = 20;

	private IClientAPI api;
	private int tickCounter;
	private int lastSignature;

	@Override
	public String getModId() {
		return Capitol.MOD_ID;
	}

	@Override
	public void initialize(IClientAPI jmClientApi) {
		this.api = jmClientApi;
		NeoForge.EVENT_BUS.addListener(this::tick);
	}

	private void tick(ClientTickEvent.Post event) {
		if (++tickCounter < REFRESH_INTERVAL_TICKS) return;
		tickCounter = 0;

		int signature = ClientClaimCache.claims.entrySet().hashCode();
		if (signature == lastSignature) return;
		lastSignature = signature;

		api.removeAll(getModId(), DisplayType.Polygon);
		for (PolygonOverlay overlay : PolygonHelper.buildClaimOverlays(getModId(), Level.OVERWORLD)) {
			try {
				api.show(overlay);
			} catch (Exception e) {
				Capitol.LOGGER.error("Failed to show claim overlay", e);
			}
		}
	}
}