package com.createcivilization.capitol.client.gui.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SmartScreen extends Screen {

	int leftPos;

	private List<Interactable> interactableList = new ArrayList<>();

	public List<Interactable> getInteractableList() {
		return interactableList;
	}

	public void setInteractableList(List<Interactable> interactableList) {
		this.interactableList = interactableList;
	}

	protected SmartScreen(Component title) {
		super(title);
	}

	protected void addInteractable(Interactable interactable) {
		interactableList.add(interactable);
	}

	protected Asset.Blit getBackgroundBlit() {
		return null;
	}

	protected int getBackgroundX() {
		return (this.width - getBackgroundBlit().getBlitWidth()) / 2;
	}

	protected int getBackgroundY() {
		return (this.height - getBackgroundBlit().getBlitHeight()) / 2;
	}

	@Override
	protected void init() {
		super.init();
		interactableList.forEach(interactable -> interactable.init(this));
	}

	public Interactable hovering;

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		if (getBackgroundBlit() instanceof Asset.Blit backgroundBlit) backgroundBlit.renderBlit(guiGraphics, getBackgroundX(), getBackgroundY());
		interactableList.forEach(interactable -> interactable.render(guiGraphics));
		Interactable currentHover = null;
		for (Interactable interactable : getInteractableList()) {
			if (!interactable.getBoundingBox().isPositionInside(mouseX, mouseY)) continue;
			interactable.hovered(mouseX, mouseY);
			currentHover = interactable;
		}

		if (currentHover == null && hovering != null) {
			hovering.hoverLeave(mouseX, mouseY);
			hovering = null;
		} else if (currentHover != null && currentHover != hovering) {
			if (hovering != null) hovering.hoverLeave(mouseX, mouseY);
			hovering = currentHover;
		}
	}

	public Interactable lastClick;

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		Interactable currentClick = null;
		if (button == 0) {
			for (Interactable interactable : interactableList) {
				if (!interactable.getBoundingBox().isPositionInside((int) mouseX, (int) mouseY)) continue;
				Interactable v = interactable.clickStart((int) mouseX, (int) mouseY);
				currentClick = v == null ? currentClick : v;
			}
			lastClick = currentClick;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0 && lastClick != null) {
			lastClick.clickRelease((int) mouseX, (int) mouseY);
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		interactableList.forEach(interactable -> interactable.input(keyCode, scanCode, modifiers));
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
