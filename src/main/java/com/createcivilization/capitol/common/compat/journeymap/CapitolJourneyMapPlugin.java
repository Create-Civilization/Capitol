package com.createcivilization.capitol.common.compat.journeymap;


import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.compat.journeymap.util.PolygonHelper;
import com.createcivilization.capitol.common.networking.packets.C2SClaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SUnclaimChunk;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.display.DisplayType;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.model.MapPolygon;
import journeymap.api.v2.client.model.ShapeProperties;
import journeymap.api.v2.client.event.PopupMenuEvent;
import journeymap.api.v2.client.fullscreen.ModPopupMenu;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

@SuppressWarnings("removal")
@JourneyMapPlugin(apiVersion = "v2")
public class CapitolJourneyMapPlugin implements IClientPlugin {

	private static final int REFRESH_INTERVAL_TICKS = 20;
	private static final int RANGE_UPDATE_INTERVAL = 2; //smoother movemnt but without overdo-ing it, change this if you like
	private static final int CLAIM_RADIUS = 7;
	private static final String RANGE_OVERLAY_ID = "claim_range_indicator";

	private IClientAPI api;
	private int tickCounter;
	private int lastSignature;
	private ChunkPos lastPlayerChunk;
	private PolygonOverlay rangeOverlay;

	@Override
	public String getModId() {
		return Capitol.MOD_ID;
	}

	@Override
	public void initialize(IClientAPI jmClientApi) {
		this.api = jmClientApi;
		NeoForge.EVENT_BUS.addListener(this::tick);
		FullscreenEventRegistry.FULLSCREEN_POPUP_MENU_EVENT.subscribe(getModId(), this::onPopupMenu);
	}

	private void onPopupMenu(PopupMenuEvent event) {
		if (event.getLayer() != PopupMenuEvent.Layer.FULLSCREEN) return;

		ModPopupMenu menu = event.getPopupMenu();
		
		menu.addMenuItem(Component.translatable("gui.journeymap.capitol.claim_chunk").getString(), (pos) -> {
			ChunkPos chunkPos = new ChunkPos(pos);
			C2SClaimChunk packet = new C2SClaimChunk(new Vector3f(chunkPos.x, 0, chunkPos.z));
			PacketDistributor.sendToServer(packet);
		});

		menu.addMenuItem(Component.translatable("gui.journeymap.capitol.unclaim_chunk").getString(), (pos) -> {
			ChunkPos chunkPos = new ChunkPos(pos);
			C2SUnclaimChunk packet = new C2SUnclaimChunk(new Vector3f(chunkPos.x, 0, chunkPos.z));
			PacketDistributor.sendToServer(packet);
		});
	}

	private void tick(ClientTickEvent.Post event) {
		if (api == null || Minecraft.getInstance().player == null) return;

		// tracking
		if (tickCounter % RANGE_UPDATE_INTERVAL == 0) {
			updateRangeOverlay();
		}

		if (++tickCounter < REFRESH_INTERVAL_TICKS) return;
		tickCounter = 0;

		int signature = ClientClaimCache.claims.entrySet().hashCode();
		if (signature == lastSignature) return;
		lastSignature = signature;

		api.removeAll(getModId(), DisplayType.Polygon);
		updateRangeOverlay(); // ensure it persists after clear

		for (PolygonOverlay overlay : PolygonHelper.buildClaimOverlays(getModId(), Level.OVERWORLD)) {
			try {
				api.show(overlay);
			} catch (Exception e) {
				Capitol.LOGGER.error("Failed to show claim overlay", e);
			}
		}
	}

	private void updateRangeOverlay() {
		var player = Minecraft.getInstance().player;
		if (player == null) return;

		ChunkPos currentChunk = player.chunkPosition();
		if (currentChunk.equals(lastPlayerChunk) && rangeOverlay != null) return;
		lastPlayerChunk = currentChunk;

		if (rangeOverlay != null) {
			api.remove(rangeOverlay);
		}

		MapPolygon poly = PolygonHelper.createRangePolygon(currentChunk, CLAIM_RADIUS);
		
		ShapeProperties props = new ShapeProperties()
			.setFillColor(0x800080) // Purple, you can change to whatever
			.setFillOpacity(0.15f)
			.setStrokeColor(0x800080)
			.setStrokeOpacity(0.4f)
			.setStrokeWidth(2f);

		rangeOverlay = new PolygonOverlay(getModId(), player.level().dimension(), props, poly);
		try {
			api.show(rangeOverlay);
		} catch (Exception e) {
			Capitol.LOGGER.error("Failed to update range overlay", e);
		}
	}
}