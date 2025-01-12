package com.createcivilization.capitol.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class GuiMenu extends Screen {

	protected static Component TITLE = Component.empty();
	private static final Component EXIT = Component.literal("X");

	private static int TITLE_WIDTH;
	protected int imageWidth, imageHeight;
	protected int leftPos;
	protected int topPos;
	protected ResourceLocation backgroundTexture;

	protected GuiMenu(Component pTitle) {
		super(pTitle);
	}

	protected void init(){
		super.init();
		TITLE_WIDTH = this.font.width(TITLE.getVisualOrderText());
	}

	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {

		this.renderBackground(guiGraphics);

		guiGraphics.blit(backgroundTexture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		guiGraphics.drawString(this.font, TITLE, (this.width - TITLE_WIDTH) / 2, this.topPos + 2, 0x404040, false);

		guiGraphics.drawString(
			this.font,
			EXIT,
			(this.width + this.imageWidth) / 2 - this.font.width(EXIT.getVisualOrderText()) - 4,
			this.topPos + 4,
			0xFFFFFF,
			false
		);

		super.render(guiGraphics, mouseX, mouseY, pPartialTick);
	}

	public boolean isPauseScreen() {
		return false;
	}
}
