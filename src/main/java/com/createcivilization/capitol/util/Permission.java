package com.createcivilization.capitol.util;

public enum Permission {
	TEAM_MEMBER_ON_TEAM_CLAIM(true, true, true, true, true),
	NON_TEAM_MEMBER_ON_TEAM_CLAIM(false, false, false, false, true),
	NON_TEAM_MEMBER_ON_SERVER_CLAIM(false, false, false, false, false);

	private final boolean
		breakBlocks,
		placeBlocks,
		useItems,
		interactEntities,
		canInteractBlocks;

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