package com.createcivilization.capitol.old.gui.base;

public class BoundingBox {
	public int startX, startY, endX, endY;

	public BoundingBox(int startX, int startY, int endX, int endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}

	public boolean isPositionInside(int x, int y) {
		return x >= startX && x <= endX && y >= startY && y <= endY;
	}

	@Override
	public String toString() {
		return startX + " " + startY + " " + endX + " " + endY;
	}
}
