package com.createcivilization.capitol.gui.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class SmartScreen extends Screen {

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

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		if (getBackgroundBlit() instanceof Asset.Blit backgroundBlit) backgroundBlit.renderBlit(guiGraphics, getBackgroundX(), getBackgroundY());
		interactableList.forEach(interactable -> interactable.render(guiGraphics));
	}

	Interactable lastClick;

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			interactableList.forEach(interactable -> {
				if (!interactable.getBoundingBox().isPositionInside((int) mouseX, (int) mouseY)) return;
				interactable.clickStart();
				lastClick = interactable;
			});
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0) {
			interactableList.forEach(interactable -> {
				if (!interactable.getBoundingBox().isPositionInside((int) mouseX, (int) mouseY) && lastClick != interactable) return;
				interactable.clickRelease();
				lastClick = null;
			});
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}
}
