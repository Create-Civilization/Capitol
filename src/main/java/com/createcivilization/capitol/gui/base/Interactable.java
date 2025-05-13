package com.createcivilization.capitol.gui.base;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.constants.ClientConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public interface Interactable {
	int getX();
	int getY();
	void setX(int x);
	void setY(int y);

	default void init(SmartScreen smartScreen) {}
	void render(GuiGraphics guiGraphics);

	BoundingBox getBoundingBox();

	default void clickStart(int x, int y) {}
	default void clickRelease(int x, int y) {}
	default void hovered(int x, int y) {}
	default void hoverLeave(int x, int y) {}
	default void scroll() {}

	void hide();
	void show();

	boolean isHidden();

	interface BlitInteractable extends Interactable{

		Asset.Blit getBlit();

		@Override
		default void render(GuiGraphics guiGraphics) {
			if (isHidden()) return;
			getBlit().renderBlit(guiGraphics, getX(), getY());
		}

		@Override
		default BoundingBox getBoundingBox() {
			int x = getX();
			int y = getY();
			return new BoundingBox(x, y, x + getBlit().getBlitWidth(), y + getBlit().getBlitHeight());
		}
	}

	class TextInteractable implements Interactable {

		boolean isHidden = false;
		int x, y;
		Component title;
		int color;
		boolean dropShadow;

		public TextInteractable(Component title, int x, int y, @Nullable Color color, @Nullable Boolean dropShadow) {
			this.title = title;
			this.color = color == null ? Color.WHITE.getRGB() : color.getRGB();
			this.dropShadow = dropShadow == null || dropShadow;
			this.x = x;
			this.y = y;
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public void setX(int x) {
			this.x = x;
		}

		@Override
		public void setY(int y) {
			this.y = y;
		}

		@Override
		public void render(GuiGraphics guiGraphics) {
			if (isHidden()) return;
			guiGraphics.drawString(ClientConstants.INSTANCE.font, this.title, this.x, this.y, this.color, this.dropShadow);
		}

		@Override
		public BoundingBox getBoundingBox() {
			return new BoundingBox(this.x, this.y, this.x + (ClientConstants.INSTANCE.font.width(this.title.getString())), this.y + (ClientConstants.INSTANCE.font.lineHeight));
		}

		@Override
		public void hide() {
			isHidden = true;
		}

		@Override
		public void show() {
			isHidden = false;
		}

		@Override
		public boolean isHidden() {
			return isHidden;
		}
	}

	abstract class InteractableBundle implements Interactable{

		public List<Interactable> interactableList = new ArrayList<>();
		private boolean isHidden;

		@Override
		public boolean isHidden() {
			return isHidden;
		}

		@Override
		public void show() {
			isHidden = false;
			getInteractableList().forEach(Interactable::show);
		}

		@Override
		public void hide() {
			isHidden = true;
			getInteractableList().forEach(Interactable::hide);
		}

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
			if (isHidden()) return;
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

		Interactable lastHover;

		@Override
		public void hovered(int x, int y) {
			getInteractableList().forEach(interactable -> {
				if (interactable.isHidden() || !interactable.getBoundingBox().isPositionInside(x,y)) return;
				if (lastHover != null) lastHover.hoverLeave(x,y);
				lastHover = interactable;
				interactable.hovered(x,y);
			});
		}

		@Override
		public void hoverLeave(int x, int y) {
			if (lastHover == null) return;
			lastHover.hoverLeave(x,y);
			lastHover = null;
		}

		@Override
		public void scroll() {
			Interactable.super.scroll();
		}


	}
}
