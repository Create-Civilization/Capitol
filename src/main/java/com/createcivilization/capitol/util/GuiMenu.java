package com.createcivilization.capitol.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiMenu extends Screen {

	protected static Component TITLE = Component.empty();
	private static final Component EXIT = Component.literal("X");

	private static int TITLE_WIDTH;
	private final List<Scene> scenes = new ArrayList<>();
	protected int imageWidth, imageHeight;
	protected int leftPos;
	protected int topPos;
	protected ResourceLocation backgroundTexture;
	protected boolean backgroundBoiler = true;

	protected GuiMenu(Component pTitle) {
		super(pTitle);
	}

	protected void swapScene(Scene from, Scene to) {
		from.hide();
		to.show();
	}

	protected void init(){
		super.init();
		TITLE_WIDTH = this.font.width(TITLE.getVisualOrderText());
		addWidget(
			Button.builder(
					Component.empty(),
					button -> this.onClose()
				)
				.bounds((this.width + this.imageWidth) / 2 - this.font.width(EXIT.getVisualOrderText()) - 4,
					this.topPos + 4,
					9,
					9)
				.build()
		);
	}

	protected void addScene(Scene scene) {
		for (AbstractWidget widget : scene.getWidgets()) {
			this.addWidget(widget);
		}
		for (AbstractWidget widget : scene.getRenderableWidgets()) {
			this.addRenderableWidget(widget);
		}
	}

	public void renderBackgroundBoiler(GuiGraphics guiGraphics){
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
	}

	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (this.backgroundBoiler) this.renderBackgroundBoiler(guiGraphics);

		for(Renderable renderable : this.renderables) {
			renderable.render(guiGraphics, mouseX, mouseY, partialTick);
		}
		for(Scene scene : this.scenes) {
			scene.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	public boolean isPauseScreen() {
		return false;
	}
}

