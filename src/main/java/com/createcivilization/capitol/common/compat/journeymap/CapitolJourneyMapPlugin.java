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