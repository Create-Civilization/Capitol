package com.createcivilization.capitol.util;

public enum Permission {
	ALL(true, true, true, true, true),
	NONE(false, false, false, false, true);

	private final boolean breakBlocks;
	private final boolean placeBlocks;
	private final boolean useItems;
	private final boolean interactEntities;
	private final boolean canInteractBlocks;

	Permission(boolean breakBlocks, boolean placeBlocks, boolean useItems, boolean interactEntities, boolean canInteractBlocks) {
		this.breakBlocks = breakBlocks;
		this.placeBlocks = placeBlocks;
		this.useItems = useItems;
		this.interactEntities = interactEntities;
		this.canInteractBlocks = canInteractBlocks;
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

	public boolean canInteractWithEntities() {
		return this.interactEntities;
	}

	public boolean canInteractBlocks() {
		return this.canInteractBlocks;
	}
}