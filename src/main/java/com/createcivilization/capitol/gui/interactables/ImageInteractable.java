package com.createcivilization.capitol.gui.interactables;

import com.createcivilization.capitol.gui.base.Asset;
import com.createcivilization.capitol.gui.base.Interactable;

public class ImageInteractable implements Interactable.BlitInteractable {

	private final Asset.Blit blit;
	private int x, y;

	public ImageInteractable(Asset.Blit blit, int x, int y) {
		this.blit = blit;
		this.x = x;
		this.y = y;
	}

	private boolean isHidden;

	@Override
	public boolean isHidden() {
		return isHidden;
	}

	@Override
	public void show() {
		isHidden = false;
	}

	@Override
	public void hide() {
		isHidden = true;
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
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}
}
