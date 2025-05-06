package com.createcivilization.capitol.gui.base;

import com.createcivilization.capitol.Capitol;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public interface Interactable {
	int getX();
	int getY();
	void setX(int x);
	void setY(int y);

	default void init(SmartScreen smartScreen) {};
	void render(GuiGraphics guiGraphics);

	BoundingBox getBoundingBox();

	default void clickStart(int x, int y) {};
	default void clickRelease(int x, int y) {};
	default void hovered() {};
	default void hoverLeave() {};
	default void scroll() {};

	interface BlitInteractable extends Interactable{

		Asset.Blit getBlit();

		@Override
		default void render(GuiGraphics guiGraphics) {
			getBlit().renderBlit(guiGraphics, getX(), getY());
		}

		@Override
		default BoundingBox getBoundingBox() {
			int x = getX();
			int y = getY();
			return new BoundingBox(x, y, x + getBlit().getBlitWidth(), y + getBlit().getBlitHeight());
		};
	}

	abstract class InteractableBundle implements Interactable{

		public List<Interactable> interactableList = new ArrayList<>();

		@Override
		public BoundingBox getBoundingBox() {
			int startX = 0;
			int startY = 0;
			int endX = 0;
			int endY = 0;

			for (Interactable interactable : getInteractableList()) {
				BoundingBox boundingBox = interactable.getBoundingBox();
				startX = Math.min(boundingBox.startX, startX);
				startY = Math.min(boundingBox.startY, startY);
				endX = Math.max(boundingBox.endX, endX);
				endY = Math.max(boundingBox.endY, endY);
			}

			return new BoundingBox(startX, startY, endX, endY);
		}

		public BoundingBox boundingBox;
		public List<Interactable> getInteractableList() {
			return this.interactableList;
		}

		public void setInteractableList(List<Interactable> interactableList) {
			this.interactableList = interactableList;
		}

		public void init(SmartScreen smartScreen) {
			getInteractableList().forEach(interactable -> {
				interactable.init(smartScreen);
				interactable.setX(interactable.getX() + getX());
				interactable.setY(interactable.getY() + getY());
			});
		}

		public void render(GuiGraphics guiGraphics) {
			getInteractableList().forEach(interactable -> interactable.render(guiGraphics));
		}

		Interactable lastClick;

		@Override
		public void clickStart(int x, int y) {
			getInteractableList().forEach(interactable -> {
				if (!interactable.getBoundingBox().isPositionInside(x, y)) return;
				interactable.clickStart(x, y);
				lastClick = interactable;
			});
		}

		@Override
		public void clickRelease(int x, int y) {
			if (lastClick == null) return;
			lastClick.clickRelease(x,y);
			lastClick = null;
		}

		@Override
		public void hovered() {
			Interactable.super.hovered();
		}

		@Override
		public void hoverLeave() {
			Interactable.super.hoverLeave();
		}

		@Override
		public void scroll() {
			Interactable.super.scroll();
		}
	}
}
