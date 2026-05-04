package com.createcivilization.capitol.common.compat.journeymap;


import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.compat.journeymap.util.PolygonHelper;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.networking.packets.C2SClaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SUnclaimChunk;
import journeymap.api.v2.client.JourneyMapPlugin;
import journeymap.api.v2.client.IClientAPI;
import journeymap.api.v2.client.IClientPlugin;
import journeymap.api.v2.client.display.DisplayType;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.model.MapPolygon;
import journeymap.api.v2.client.model.ShapeProperties;
import journeymap.api.v2.client.event.FullscreenDisplayEvent;
import journeymap.api.v2.client.event.FullscreenMapEvent;
import journeymap.api.v2.client.event.PopupMenuEvent;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("removal")
@JourneyMapPlugin(apiVersion = "v2")
public class CapitolJourneyMapPlugin implements IClientPlugin {

	private static final int REFRESH_INTERVAL_TICKS = 20;
	private static final int RANGE_UPDATE_INTERVAL = 2; //smoother movemnt but without overdo-ing it, change this if you like

	private IClientAPI api;
	private int tickCounter;
	private int lastSignature;
	private ChunkPos lastPlayerChunk;
	private ChunkPos lastAreaCenterChunk;
	private PolygonOverlay rangeOverlay;
	private boolean rangeVisible;

	private boolean claimingMode;
	private boolean tracking;
	private int trackingButton;
	private final Set<ChunkPos> area = new HashSet<>();
	private final Set<ChunkPos> selected = new HashSet<>();
	private final Map<ChunkPos, PolygonOverlay> selectedOverlays = new HashMap<>();
	private ResourceKey<Level> selectionDimension;
	private boolean lastRmbDown;
	private boolean suppressPopupMenuOnce;

	@Override
	public String getModId() {
		return Capitol.MOD_ID;
	}

	@Override
	public void initialize(IClientAPI jmClientApi) {
		this.api = jmClientApi;
		NeoForge.EVENT_BUS.addListener(this::tick);
		FullscreenEventRegistry.FULLSCREEN_MAP_DRAG_EVENT.subscribe(getModId(), this::onMapDrag);
		FullscreenEventRegistry.FULLSCREEN_MAP_MOVE_EVENT.subscribe(getModId(), this::onMapMove);
		FullscreenEventRegistry.FULLSCREEN_POPUP_MENU_EVENT.subscribe(getModId(), this::onPopupMenu);
		FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(getModId(), this::onAddonButtonDisplay);
	}

	private void onAddonButtonDisplay(FullscreenDisplayEvent.AddonButtonDisplayEvent event) {
		ResourceLocation icon = ResourceLocation.fromNamespaceAndPath("journeymap", "theme/flat/icon/grid.png");
		event.getThemeButtonDisplay().addThemeToggleButton("Claim Mode", icon, claimingMode, button -> {
			claimingMode = !claimingMode;
			button.setToggled(claimingMode);
			if (claimingMode) {
				rangeVisible = true;
				updateRangeOverlay();
				ensureAreaUpToDate();
			} else {
				tracking = false;
				trackingButton = -1;
				selectionDimension = null;
				lastRmbDown = false;
				suppressPopupMenuOnce = false;
				clearSelection();
				hideRangeOverlay();
			}
		});
	}

	private void onMapDrag(FullscreenMapEvent.MouseDraggedEvent event) {
		if (!claimingMode) return;
		if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;

		var player = Minecraft.getInstance().player;
		if (player == null) return;

		selectionDimension = event.getLevel();
		ensureAreaUpToDate();

		ChunkPos chunkPos = new ChunkPos(event.getLocation());
		if (!area.contains(chunkPos)) {
			if (tracking && event.getStage() == FullscreenMapEvent.Stage.PRE) {
				event.cancel();
			}
			return;
		}

		if (!tracking) {
			tracking = true;
			trackingButton = event.getButton();
			lastRmbDown = true;
		}

		addSelectedChunk(chunkPos);
		if (event.getStage() == FullscreenMapEvent.Stage.PRE) {
			event.cancel();
		}
	}

	private void onMapMove(FullscreenMapEvent.MouseMoveEvent event) {
		if (!claimingMode) return;

		boolean rmbDown = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		if (!tracking) {
			if (!rmbDown) return;
			selectionDimension = event.getLevel();
			ensureAreaUpToDate();

			ChunkPos startPos = new ChunkPos(event.getLocation());
			if (!area.contains(startPos)) return;

			tracking = true;
			trackingButton = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
			lastRmbDown = true;
			addSelectedChunk(startPos);
			return;
		}

		if (trackingButton != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
		if (!rmbDown) return;

		ChunkPos chunkPos = new ChunkPos(event.getLocation());
		if (!area.contains(chunkPos)) return;

		addSelectedChunk(chunkPos);
	}

	private void onPopupMenu(PopupMenuEvent event) {
		if (event.getLayer() != PopupMenuEvent.Layer.FULLSCREEN) return;
		if (!claimingMode) return;
		if (tracking || suppressPopupMenuOnce) {
			event.cancel();
			suppressPopupMenuOnce = false;
		}
	}

	private void tick(ClientTickEvent.Post event) {
		if (api == null || Minecraft.getInstance().player == null) return;

		if (claimingMode) {
			ensureAreaUpToDate();
		}

		if (tracking) {
			boolean down = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
			if (lastRmbDown && !down) {
				tracking = false;
				trackingButton = -1;
				suppressPopupMenuOnce = true;
				Minecraft.getInstance().setScreen(new ClaimActionScreen(Minecraft.getInstance().screen));
			}
			lastRmbDown = down;
			return;
		}

		if (rangeVisible && tickCounter % RANGE_UPDATE_INTERVAL == 0) {
			updateRangeOverlay();
		}

		if (++tickCounter < REFRESH_INTERVAL_TICKS) return;
		tickCounter = 0;

		int signature = ClientClaimCache.claims.entrySet().hashCode();
		if (signature == lastSignature) return;
		lastSignature = signature;

		api.removeAll(getModId(), DisplayType.Polygon);
		if (rangeVisible) updateRangeOverlay();
		for (PolygonOverlay overlay : selectedOverlays.values()) {
			try {
				api.show(overlay);
			} catch (Exception e) {
				Capitol.LOGGER.error("Failed to show selection overlay", e);
			}
		}

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

		if (!rangeVisible) return;

		int claimRadius = CapitolConfig.CLAIM_RADIUS.get();
		if (claimRadius <= 0) return;

		ChunkPos currentChunk = player.chunkPosition();
		if (currentChunk.equals(lastPlayerChunk) && rangeOverlay != null) return;
		lastPlayerChunk = currentChunk;

		if (rangeOverlay != null) {
			try {
				api.remove(rangeOverlay);
			} catch (Exception ignored) {
			}
		}

		MapPolygon poly = PolygonHelper.createRangePolygon(currentChunk, claimRadius);
		
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

	private void hideRangeOverlay() {
		rangeVisible = false;
		lastPlayerChunk = null;
		lastAreaCenterChunk = null;
		if (rangeOverlay == null) return;
		try {
			api.remove(rangeOverlay);
		} catch (Exception ignored) {
		}
		rangeOverlay = null;
	}

	private void ensureAreaUpToDate() {
		var player = Minecraft.getInstance().player;
		if (player == null) return;

		int claimRadius = CapitolConfig.CLAIM_RADIUS.get();
		if (claimRadius <= 0) {
			area.clear();
			lastAreaCenterChunk = null;
			return;
		}

		ChunkPos currentChunk = player.chunkPosition();
		if (currentChunk.equals(lastAreaCenterChunk) && !area.isEmpty()) return;

		lastAreaCenterChunk = currentChunk;
		area.clear();
		for (int dx = -claimRadius; dx <= claimRadius; dx++) {
			for (int dz = -claimRadius; dz <= claimRadius; dz++) {
				area.add(new ChunkPos(currentChunk.x + dx, currentChunk.z + dz));
			}
		}
	}

	private void addSelectedChunk(ChunkPos chunkPos) {
		if (!selected.add(chunkPos)) return;

		ResourceKey<Level> dim = selectionDimension;
		if (dim == null) {
			var player = Minecraft.getInstance().player;
			if (player == null) return;
			dim = player.level().dimension();
		}

		MapPolygon poly = PolygonHelper.createRangePolygon(chunkPos, 0);
		ShapeProperties props = new ShapeProperties()
			.setFillColor(0xFFFFFF)
			.setFillOpacity(0.35f)
			.setStrokeColor(0xFFFFFF)
			.setStrokeOpacity(0.85f)
			.setStrokeWidth(2f);

		PolygonOverlay overlay = new PolygonOverlay(getModId(), dim, props, poly);
		selectedOverlays.put(chunkPos, overlay);
		try {
			api.show(overlay);
		} catch (Exception e) {
			Capitol.LOGGER.error("Failed to show selection overlay", e);
		}
	}

	private void clearSelection() {
		selected.clear();
		for (PolygonOverlay overlay : selectedOverlays.values()) {
			try {
				api.remove(overlay);
			} catch (Exception ignored) {
			}
		}
		selectedOverlays.clear();
	}

	private class ClaimActionScreen extends Screen {
		private final Screen parent;

		protected ClaimActionScreen(Screen parent) {
			super(Component.literal("Capitol Claiming"));
			this.parent = parent;
		}

		@Override
		protected void init() {
			int buttonWidth = 140;
			int buttonHeight = 20;
			int x = (this.width - buttonWidth) / 2;
			int y = (this.height - (buttonHeight * 2 + 6)) / 2;

			this.addRenderableWidget(Button.builder(Component.literal("Claim chunks"), b -> {
				for (ChunkPos pos : selected) {
					PacketDistributor.sendToServer(new C2SClaimChunk(pos.toLong()));
				}
				clearSelection();
				Minecraft.getInstance().setScreen(parent);
			}).bounds(x, y, buttonWidth, buttonHeight).build());

			this.addRenderableWidget(Button.builder(Component.literal("Unclaim chunks"), b -> {
				for (ChunkPos pos : selected) {
					PacketDistributor.sendToServer(new C2SUnclaimChunk(pos.toLong()));
				}
				clearSelection();
				Minecraft.getInstance().setScreen(parent);
			}).bounds(x, y + buttonHeight + 6, buttonWidth, buttonHeight).build());
		}

		@Override
		public void onClose() {
			clearSelection();
			Minecraft.getInstance().setScreen(parent);
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}
	}
}
