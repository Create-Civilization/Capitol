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
) {}