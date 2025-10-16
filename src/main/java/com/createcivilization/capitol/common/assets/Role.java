package com.createcivilization.capitol.common.assets;

import com.createcivilization.capitol.old.config.CapitolConfig;

public record Role(
	String name,
	boolean invitePlayers,
	boolean breakBlocks,
	boolean placeBlocks,
	boolean useItems,
	boolean interactEntities,
	boolean interactBlocks,
	boolean editRoles,
	boolean editPermissions,
	boolean removeMember,
	boolean declareWar
) {
	public static Role ownerRole() {
		return new Role(
			"owner",
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true
		);
	}
	public static Role moderatorRole() {
		return new Role(
			"moderator",
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			false
		);
	}
	public static Role memberRole() {
		return new Role(
			"member",
			true,
			true,
			true,
			true,
			true,
			true,
			false,
			false,
			false,
			false
		);
	}
	public static Role guestRole() {
		return new Role(
			"guest",
			false,
			false,
			false,
			CapitolConfig.SERVER.nonMemberUseItems.get(),
			CapitolConfig.SERVER.nonMemberInteractEntities.get(),
			CapitolConfig.SERVER.nonMemberInteractBlocks.get(),
			false,
			false,
			false,
			false
		);
	}
	public static Role emptyRole(String name) {
		return new Role(
			name,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false,
			false
		);
	}
}
