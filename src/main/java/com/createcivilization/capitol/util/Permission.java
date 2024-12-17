package com.createcivilization.capitol.util;

public enum Permission {
	ALL(true, true, true, true),
	NONE(false, false, false, false);

	private final boolean breakBlocks;
	private final boolean placeBlocks;
	private final boolean useItems;
	private final boolean interactEntities;

	Permission(boolean breakBlocks, boolean placeBlocks, boolean useItems, boolean interactEntities) {
		this.breakBlocks = breakBlocks;
		this.placeBlocks = placeBlocks;
		this.useItems = useItems;
		this.interactEntities = interactEntities;
	}

	public boolean canBreakBlocks() {
		return this.breakBlocks;
	}

	public boolean canPlaceBlocks() {
		return this.placeBlocks;
	}

	public boolean canUseItems() {
		return this.useItems;
	}

	public boolean canInteractEntities() {
		return this.interactEntities;
	}
}