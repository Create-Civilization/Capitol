package com.createcivilization.capitol.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Scene {
	private final List<AbstractWidget> widgets = new ArrayList<>();
	private final List<AbstractWidget> renderableWidgets = new ArrayList<>();

	public void hideWidget(AbstractWidget widget) {
		widget.active = false;
		widget.visible = false;
	}

	public void showWidget(AbstractWidget widget) {
		widget.active = true;
		widget.visible = true;
	}

	public List<AbstractWidget> getWidgets() {
		return widgets;
	}
	public List<AbstractWidget> getRenderableWidgets() {
		return renderableWidgets;
	}

	public <T extends GuiEventListener & Renderable & NarratableEntry> T addWidget(T widget) {
		this.widgets.add((AbstractWidget) widget);
		return widget;
	}
	public <T extends GuiEventListener & NarratableEntry> T addRenderableWidget(T widget) {
		this.renderableWidgets.add((AbstractWidget) widget);
		return widget;
	}

	public void hide() {
		this.widgets.forEach(widget -> {
			widget.visible = false;
			widget.active = false;
		});
		this.renderableWidgets.forEach(widget -> {
			widget.visible = false;
			widget.active = false;
		});
	}

	public void show() {
		this.widgets.forEach(widget -> {
			widget.visible = true;
			widget.active = true;
		});
		this.renderableWidgets.forEach(widget -> {
			widget.visible = true;
			widget.active = true;
		});
	}

	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		for(Renderable renderable : this.widgets) {
			renderable.render(guiGraphics, mouseX, mouseY, partialTick);
		}
	}
}
