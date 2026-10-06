package com.createcivilization.capitol.client.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.item.SubClaimWand;
import com.createcivilization.capitol.common.networking.packets.C2SDamageWand;

import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

public class SubClaimNamingScreen extends Screen {

	private static final Component TITLE = Component.translatable("screen.capitol.sub_claim_naming.title");
	private static final Component PLACEHOLDER = Component.translatable("screen.capitol.sub_claim_naming.placeholder");
	private static final Component CANCEL = Component.translatable("screen.capitol.sub_claim_naming.cancel");
	private static final Component CONFIRM = Component.translatable("screen.capitol.sub_claim_naming.confirm");

	private static final int PANEL_WIDTH = 220;
	private static final int PANEL_HEIGHT = 80;
	private static final int EDIT_BOX_WIDTH = 200;
	private static final int EDIT_BOX_HEIGHT = 20;
	private static final int BUTTON_WIDTH = 96;
	private static final int BUTTON_HEIGHT = 20;
	private static final int INSET = 10;

	private final BlockPos firstPos;
	private final BlockPos secondPos;
	private final ItemStack wandStack;

	private EditBox nameField;

	public SubClaimNamingScreen(BlockPos firstPos, BlockPos secondPos, ItemStack wandStack) {
		super(TITLE);
		this.firstPos = firstPos;
		this.secondPos = secondPos;
		this.wandStack = wandStack;
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

		Capitol.LOGGER.info("Sub-claim '{}' confirmed: {} -> {}", name, this.firstPos, this.secondPos);
		SubClaimWand.clearSelection(this.wandStack);
		PacketDistributor.sendToServer(new C2SDamageWand());
		onClose();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics, mouseX, mouseY, partialTick);

		int panelX = (this.width - PANEL_WIDTH) / 2;
		int panelY = (this.height - PANEL_HEIGHT) / 2;

		graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, 0xCC000000);

		super.render(graphics, mouseX, mouseY, partialTick);

		graphics.drawCenteredString(this.font, TITLE, this.width / 2, panelY - this.font.lineHeight - INSET / 2, 0xFFFFFFFF);
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
