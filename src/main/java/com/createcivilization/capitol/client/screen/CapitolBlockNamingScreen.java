package com.createcivilization.capitol.client.screen;

import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.networking.packets.C2SCancelCapitolBlockNaming;
import com.createcivilization.capitol.common.networking.packets.C2SNameCapitolBlock;
import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolNamingScreen;

import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

// shown right after placing a capitol block: the player picks its name,
// which has to be globally unique across every team
public class CapitolBlockNamingScreen extends Screen {

	private static final Component CANCEL = Component.literal("Cancel");
	private static final Component CONFIRM = Component.literal("Confirm");
	private static final Component PLACEHOLDER = Component.literal("Name your block...");

	private static final int PANEL_WIDTH = 220;
	private static final int PANEL_HEIGHT = 80;
	private static final int EDIT_BOX_WIDTH = 200;
	private static final int EDIT_BOX_HEIGHT = 20;
	private static final int BUTTON_WIDTH = 96;
	private static final int BUTTON_HEIGHT = 20;
	private static final int INSET = 10;

	private final S2COpenCapitolNamingScreen payload;

	private EditBox nameField;
	// false once we've confirmed, so closing the screen doesn't also cancel
	private boolean pending = true;

	public CapitolBlockNamingScreen(S2COpenCapitolNamingScreen payload) {
		super(Component.literal(payload.isCapital() ? "Name your Capital" : "Name your " + niceName(payload.tier())));
		this.payload = payload;
	}

	@Override
	protected void init() {
		int panelX = (this.width - PANEL_WIDTH) / 2;
		int panelY = (this.height - PANEL_HEIGHT) / 2;

		this.nameField = new EditBox(
			this.font,
			panelX + (PANEL_WIDTH - EDIT_BOX_WIDTH) / 2,
			panelY + INSET,
			EDIT_BOX_WIDTH,
			EDIT_BOX_HEIGHT,
			Component.empty()
		);
		this.nameField.setMaxLength(32);
		this.nameField.setHint(PLACEHOLDER);
		this.addRenderableWidget(this.nameField);

		this.addRenderableWidget(
			Button.builder(CANCEL, button -> onClose())
				.bounds(panelX + INSET, panelY + PANEL_HEIGHT - BUTTON_HEIGHT - INSET, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build()
		);

		this.addRenderableWidget(
			Button.builder(CONFIRM, button -> confirm())
				.bounds(panelX + PANEL_WIDTH - BUTTON_WIDTH - INSET, panelY + PANEL_HEIGHT - BUTTON_HEIGHT - INSET, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build()
		);

		this.setInitialFocus(this.nameField);
	}

	private void confirm() {
		String name = this.nameField.getValue().trim();
		if (name.isBlank()) {
			this.nameField.setTextColor(0xFF0000);
			return;
		}

		pending = false;
		PacketDistributor.sendToServer(new C2SNameCapitolBlock(payload.pos(), name, payload.teamId()));
		onClose();
	}

	@Override
	public void onClose() {
		// closing without confirming means the block is removed and refunded
		if (pending) {
			PacketDistributor.sendToServer(new C2SCancelCapitolBlockNaming(payload.pos(), payload.teamId()));
		}
		super.onClose();
	}

	private static String niceName(@org.jetbrains.annotations.Nullable CapitolTier tier) {
		if (tier == null) return "Village";
		return Character.toUpperCase(tier.name().charAt(0)) + tier.name().substring(1).toLowerCase();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics, mouseX, mouseY, partialTick);

		int panelX = (this.width - PANEL_WIDTH) / 2;
		int panelY = (this.height - PANEL_HEIGHT) / 2;

		graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xCC000000);

		super.render(graphics, mouseX, mouseY, partialTick);

		graphics.drawCenteredString(this.font, this.title, this.width / 2, panelY - this.font.lineHeight - INSET / 2, 0xFFFFFFFF);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			confirm();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}