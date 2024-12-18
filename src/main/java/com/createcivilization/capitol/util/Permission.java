package com.createcivilization.capitol.util;

/**
 * Chunk permissions for players.
 */
public enum Permission {

	TEAM_MEMBER_ON_TEAM_CLAIM(true, true, true, true, true),
	NON_TEAM_MEMBER_ON_TEAM_CLAIM(false, false, true, false, true),
	NON_TEAM_MEMBER_ON_SERVER_CLAIM(false, false, false, false, false);

	private final boolean
			breakBlocks,
			placeBlocks,
			useItems, // Except for ender pearls and boats
			interactEntities,
			canInteractBlocks;

	Permission(boolean breakBlocks, boolean placeBlocks, boolean useItems, boolean interactEntities, boolean canInteractBlocks) {
		this.breakBlocks = breakBlocks;
		this.placeBlocks = placeBlocks;
		this.useItems = useItems;
		this.interactEntities = interactEntities;
		this.canInteractBlocks = canInteractBlocks;
	}

	/**
	 * @return If the player can break blocks in the targeted chunk.
	 */
	public boolean canBreakBlocks() {
		return this.breakBlocks;
	}

	/**
	 * @return If the player can place blocks in the targeted chunk.
	 */
	public boolean canPlaceBlocks() {
		return this.placeBlocks;
	}

	/**
	 * @return If the player can use items in the targeted chunk.
	 */
	public boolean canUseItems() {
		return this.useItems;
	}

	/**
	 * @return If the player can interact with entities in the targeted chunk.
	 */
	public boolean canInteractWithEntities() {
		return this.interactEntities;
	}

	/**
	 * @return If the player can interact with blocks in the targeted chunk.
	 */
	public boolean canInteractBlocks() {
		return this.canInteractBlocks;
	}
}