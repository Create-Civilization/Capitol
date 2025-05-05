package com.createcivilization.capitol.gui.interactables;

import com.createcivilization.capitol.gui.base.Asset;
import com.createcivilization.capitol.gui.base.BoundingBox;
import com.createcivilization.capitol.gui.base.Interactable;
import com.createcivilization.capitol.gui.base.SmartScreen;

public class ButtonInteractable implements Interactable.BlitInteractable {

	private final Asset.Blit blit;
	private final int x, y;

	public ButtonInteractable(Asset.Blit blit, int x, int y) {
		this.blit = blit;
		this.x = x;
		this.y = y;
	}

	@Override
	public Asset.Blit getBlit() {
		return blit;
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
	public void clickStart() {
		this.blit.setPosition(148, null);
	}

	@Override
	public void clickRelease() {
		this.blit.setPosition(42, null);
	}
}
