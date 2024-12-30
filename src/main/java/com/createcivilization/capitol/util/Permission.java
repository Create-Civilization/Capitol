package com.createcivilization.capitol.util;

public record Permission(
	boolean breakBlocks,
	boolean placeBlocks,
	boolean useItems,
	boolean interactEntities,
	boolean interactBlocks,
	boolean addRole,
	boolean editPermissions,
	boolean promotePlayers,
	boolean demotePlayers
) {
	public static final Permission NONE_REFERENCE = new Permission(true, true, true, true, true, true, true, true, true);
}