package com.createcivilization.capitol.gui.base;

import net.minecraft.client.gui.GuiGraphics;

public interface Interactable {

	default void init(SmartScreen smartScreen) {};
	void render(GuiGraphics guiGraphics);

	BoundingBox getBoundingBox();

	default void clickStart() {};
	default void clickRelease() {};
	default void hovered() {};
	default void hoverLeave() {};
	default void scroll() {};

	interface BlitInteractable extends Interactable{

		Asset.Blit getBlit();
		int getX();
		int getY();

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
}
