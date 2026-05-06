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
import journeymap.api.v2.client.fullscreen.ModPopupMenu;
import journeymap.api.v2.client.fullscreen.IThemeButton;
import journeymap.api.v2.common.event.FullscreenEventRegistry;
import net.minecraft.client.Minecraft;
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
	private static final Class<?> FULLSCREEN_MAP_SCREEN_CLASS = resolveFullscreenMapScreenClass();
	private static final String MOD_ID = Capitol.MOD_ID;

	private IClientAPI api;
	private int tickCounter;
	private int lastSignature;
	private ChunkPos lastPlayerChunk;
	private ChunkPos lastAreaCenterChunk;
	private PolygonOverlay rangeOverlay;
	private boolean rangeVisible;

	private boolean claimingMode;
	// tracking means the user is currently doing a right-click drag selection
	private boolean tracking;
	private int trackingButton;
	private final Set<ChunkPos> area = new HashSet<>();
	private final Set<ChunkPos> selected = new HashSet<>();
	// we keep these so we can remove the highlight overlays later
	private final Map<ChunkPos, PolygonOverlay> selectedOverlays = new HashMap<>();
	private ResourceKey<Level> selectionDimension;
	private boolean lastRmbDown;
	private boolean pendingSelectionMenu;
	private int lastClickButton = -1;

	private IThemeButton claimModeButton;
	private IThemeButton claimSelectedButton;
	private IThemeButton unclaimSelectedButton;

	@Override
	public String getModId() {
		return MOD_ID;
	}

	@Override
	public void initialize(IClientAPI jmClientApi) {
		this.api = jmClientApi;
		NeoForge.EVENT_BUS.addListener(this::tick);
		FullscreenEventRegistry.FULLSCREEN_MAP_CLICK_EVENT.subscribe(MOD_ID, this::onMapClick);
		FullscreenEventRegistry.FULLSCREEN_MAP_DRAG_EVENT.subscribe(MOD_ID, this::onMapDrag);
		FullscreenEventRegistry.FULLSCREEN_MAP_MOVE_EVENT.subscribe(MOD_ID, this::onMapMove);
		FullscreenEventRegistry.FULLSCREEN_POPUP_MENU_EVENT.subscribe(MOD_ID, this::onPopupMenu);
		FullscreenEventRegistry.ADDON_BUTTON_DISPLAY_EVENT.subscribe(MOD_ID, this::onAddonButtonDisplay);
	}

	// this runs when journeymap builds the fullscreen ui buttons
	// we add claim mode + claim/unclaim buttons here
	private void onAddonButtonDisplay(FullscreenDisplayEvent.AddonButtonDisplayEvent event) {
		ResourceLocation icon = ResourceLocation.fromNamespaceAndPath("journeymap", "theme/flat/icon/grid.png");

		claimModeButton = event.getThemeButtonDisplay().addThemeToggleButton("Claim Mode", icon, claimingMode, button -> {
			claimingMode = !claimingMode;
			button.setToggled(claimingMode);
			updateClaimModeState();
		});

		claimSelectedButton = event.getThemeButtonDisplay().addThemeToggleButton("Claim", icon, false, button -> {
			button.setToggled(false);
			claimSelected();
		});

		unclaimSelectedButton = event.getThemeButtonDisplay().addThemeToggleButton("Unclaim", icon, false, button -> {
			button.setToggled(false);
			unclaimSelected();
		});

		updateClaimModeUiState();
	}

	// left click selects exactly one chunk (if its in range), and we let the popup menu show claim/unclaim
	// right click should not do anything here because rmb is for drag selection only
	private void onMapClick(FullscreenMapEvent.ClickEvent event) {
		lastClickButton = event.getButton();

		if (!claimingMode || tracking) return;

		if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			selectionDimension = event.getLevel();
			ensureAreaUpToDate();

			ChunkPos chunkPos = new ChunkPos(event.getLocation());
			if (!area.contains(chunkPos)) return;

			clearSelection();
			addSelectedChunk(chunkPos);
			pendingSelectionMenu = true;
		}
	}

	// this is the "real" rmb drag hook when journeymap fires a drag event
	// we cancel the event so the map doesn't pan while we're selecting
	private void onMapDrag(FullscreenMapEvent.MouseDraggedEvent event) {
		if (!claimingMode || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;

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
			clearSelection();
			tracking = true;
			trackingButton = event.getButton();
			lastRmbDown = true;
			pendingSelectionMenu = false;
		}

		addSelectedChunk(chunkPos);
		if (event.getStage() == FullscreenMapEvent.Stage.PRE) {
			event.cancel();
		}
	}

	// some journeymap builds are weird about drag events, so this is a backup
	// if rmb is held and the mouse moves over new chunks, we add them to selection too
	private void onMapMove(FullscreenMapEvent.MouseMoveEvent event) {
		if (!claimingMode) return;

		boolean rmbDown = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
		if (!tracking) {
			if (!rmbDown) return;
			selectionDimension = event.getLevel();
			ensureAreaUpToDate();

			ChunkPos startPos = new ChunkPos(event.getLocation());
			if (!area.contains(startPos)) return;

			clearSelection();
			tracking = true;
			trackingButton = GLFW.GLFW_MOUSE_BUTTON_RIGHT;
			lastRmbDown = true;
			pendingSelectionMenu = false;
			addSelectedChunk(startPos);
			return;
		}

		if (trackingButton != GLFW.GLFW_MOUSE_BUTTON_RIGHT || !rmbDown) return;


		ChunkPos chunkPos = new ChunkPos(event.getLocation());
		if (!area.contains(chunkPos)) return;

		addSelectedChunk(chunkPos);
	}

	// we only want the popup menu for left click single selection
	// if someone right clicks in claim mode, we cancel the menu so rmb stays "selection only"
	private void onPopupMenu(PopupMenuEvent event) {
		if (event.getLayer() != PopupMenuEvent.Layer.FULLSCREEN) return;
		if (!claimingMode) return;

		if (lastClickButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT || tracking) {
			event.cancel();
			return;
		}

		if (!pendingSelectionMenu || selected.isEmpty()) return;
		pendingSelectionMenu = false;

		ModPopupMenu menu = event.getPopupMenu();

		menu.addMenuItem(Component.translatable("gui.journeymap.capitol.claim_chunk").getString(), (pos) -> {
			long[] packed = selected.stream().mapToLong(ChunkPos::toLong).toArray();
			PacketDistributor.sendToServer(new C2SClaimChunk(packed));
			clearSelection();
		});

		menu.addMenuItem(Component.translatable("gui.journeymap.capitol.unclaim_chunk").getString(), (pos) -> {
			long[] packed = selected.stream().mapToLong(ChunkPos::toLong).toArray();
			PacketDistributor.sendToServer(new C2SUnclaimChunk(packed));
			clearSelection();
		});
	}

	// main client loop for journeymap overlays
	// important to only show the claim radius overlay on the fullscreen map, not on the minimap
	private void tick(ClientTickEvent.Post event) {
		if (api == null || Minecraft.getInstance().player == null) return;

		boolean fullscreenMapOpen = isFullscreenMapOpen();

		if (claimingMode && fullscreenMapOpen) {
			if (!rangeVisible) {
				rangeVisible = true;
				updateRangeOverlay();
			}
			ensureAreaUpToDate();
		} else {
			if (rangeVisible) {
				hideRangeOverlay();
			}
			if (tracking) {
				tracking = false;
				trackingButton = -1;
				lastRmbDown = false;
			}
			if (!selected.isEmpty()) {
				clearSelection();
			}
			pendingSelectionMenu = false;
			selectionDimension = null;
		}

		if (tracking && fullscreenMapOpen) {
			boolean down = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
			if (lastRmbDown && !down) {
				tracking = false;
				trackingButton = -1;
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

		// remove all our polygons then rebuild them from our caches
		// we re-show the selected overlays because removeAll will wipe them too
		api.removeAll(MOD_ID, DisplayType.Polygon);
		if (rangeVisible) updateRangeOverlay();
		for (PolygonOverlay overlay : selectedOverlays.values()) {
			try {
				api.show(overlay);
			} catch (Exception e) {
				Capitol.LOGGER.error("Failed to show selection overlay", e);
			}
		}

		for (PolygonOverlay overlay : PolygonHelper.buildClaimOverlays(MOD_ID, Level.OVERWORLD)) {
			try {
				api.show(overlay);
			} catch (Exception e) {
				Capitol.LOGGER.error("Failed to show claim overlay", e);
			}
		}
	}

	// hard reset when claim mode toggles off
	// also makes sure we instantly show the purple radius when toggling on
	private void updateClaimModeState() {
		if (claimingMode) {
			rangeVisible = true;
			updateRangeOverlay();
			ensureAreaUpToDate();
		} else {
			tracking = false;
			trackingButton = -1;
			selectionDimension = null;
			lastRmbDown = false;
			pendingSelectionMenu = false;
			clearSelection();
			hideRangeOverlay();
		}
		updateClaimModeUiState();
	}

	// enables/disables buttons based on claim mode and if we have anything selected
	private void updateClaimModeUiState() {
		if (claimModeButton != null) {
			try {
				claimModeButton.setToggled(claimingMode);
			} catch (Throwable ignored) {
			}
		}

		boolean actionEnabled = claimingMode && !selected.isEmpty();
		if (claimSelectedButton != null) {
			try {
				claimSelectedButton.setEnabled(actionEnabled);
			} catch (Throwable ignored) {
			}
		}
		if (unclaimSelectedButton != null) {
			try {
				unclaimSelectedButton.setEnabled(actionEnabled);
			} catch (Throwable ignored) {
			}
		}
	}

	// this sends a single packet for the whole selection, so chat doesn't get spammed
	private void claimSelected() {
		if (selected.isEmpty()) return;
		long[] packed = selected.stream().mapToLong(ChunkPos::toLong).toArray();
		PacketDistributor.sendToServer(new C2SClaimChunk(packed));
		clearSelection();
		updateClaimModeUiState();
	}

	// same as claimSelected but for unclaim
	private void unclaimSelected() {
		if (selected.isEmpty()) return;
		long[] packed = selected.stream().mapToLong(ChunkPos::toLong).toArray();
		PacketDistributor.sendToServer(new C2SUnclaimChunk(packed));
		clearSelection();
		updateClaimModeUiState();
	}

	// only want the radius overlay in fullscreen, not in minimap
	private boolean isFullscreenMapOpen() {
		var screen = Minecraft.getInstance().screen;
		if (screen == null) return false;
		return FULLSCREEN_MAP_SCREEN_CLASS != null && FULLSCREEN_MAP_SCREEN_CLASS.isInstance(screen);
	}

	// this only runs once (cached), so we dont do the class lookup every tick
	private static Class<?> resolveFullscreenMapScreenClass() {
		try {
			return Class.forName("journeymap.api.v2.client.fullscreen.IFullscreen");
		} catch (Throwable ignored) {
			return null;
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

		rangeOverlay = new PolygonOverlay(MOD_ID, player.level().dimension(), props, poly);
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

	// builds the "in range" chunk set around the player using config claim radius
	// we rebuild only when player changes chunk, so its cheap on memory
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

	// adds a selected chunk and shows a white highlight overlay for it
	// if its already selected, nothing happens
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

		PolygonOverlay overlay = new PolygonOverlay(MOD_ID, dim, props, poly);
		selectedOverlays.put(chunkPos, overlay);
		try {
			api.show(overlay);
		} catch (Exception e) {
			Capitol.LOGGER.error("Failed to show selection overlay", e);
		}

		updateClaimModeUiState();
	}

	// removes all selection highlights + clears the selected chunk list
	private void clearSelection() {
		selected.clear();
		for (PolygonOverlay overlay : selectedOverlays.values()) {
			try {
				api.remove(overlay);
			} catch (Exception ignored) {
			}
		}
		selectedOverlays.clear();
		updateClaimModeUiState();
	}
}
