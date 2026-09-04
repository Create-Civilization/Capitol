package com.createcivilization.capitol.common.data;

public enum Permission {
	// --- Administrative ---
	CLAIM_CHUNKS(1L),
	UNCLAIM_CHUNKS(1L << 1),
	FORCELOAD_CHUNKS(1L << 2),
	INVITE_MEMBERS(1L << 3),
	KICK_MEMBERS(1L << 4),
	MANAGE_ROLES(1L << 5),  // create/edit/delete roles
	ASSIGN_ROLES(1L << 6),  // change a member's role
	MANAGE_TEAM(1L << 7),  // rename team, disband, etc.

	// --- Block Protection ---
	BREAK_BLOCKS(1L << 8),
	PLACE_BLOCKS(1L << 9),
	INTERACT_BLOCKS(1L << 10), // doors, buttons, levers, etc.
	OPEN_CONTAINERS(1L << 11), // chests, barrels, hoppers, furnaces
	CROP_TRAMPLE(1L << 12),
	FROST_WALKING(1L << 13),

	// --- Entity Protection ---
	/// Interaction with all entities (including non-living) – villagers, item frames, armour stands, etc.
	INTERACT_ENTITIES(1L << 14),
	/// Whether passive entities can be attacked.
	ATTACK_PASSIVE(1L << 15),
	/// Whether hostile entities can be attacked.
	ATTACK_HOSTILE(1L << 16),
	/// Whether players can be attacked.
	ATTACK_PLAYER(1L << 17),

	// --- Item Protection ---
	/// Whether items can be used.
	USE_ITEMS(1L << 18),
	/// Whether dropped items can be picked up.
	PICKUP_ITEMS(1L << 19),
	/// Whether XP can be picked up.
	PICKUP_XP(1L << 20),
	/// Whether items can be dropped.
	DROP_ITEMS(1L << 21),
	/// Whether mobs drop loot on death.
	MOB_LOOT(1L << 22),
	/// Whether items dropped by a player on death can be picked up.
	PLAYER_DEATH_LOOT(1L << 23),

	// --- Redstone / Mechanical ---
	INTERACT_REDSTONE(1L << 24), // pressure plates, tripwires, buttons, pistons, dispensers, etc.

	// --- Portal / Teleportation ---
	USE_NETHER_PORTALS(1L << 25),
	CHORUS_FRUIT_TELEPORT(1L << 26),

	// --- Sub-Claims ---
	CLAIM_SUB_CLAIMS(1L << 27);

	private final long flag;

	Permission(long flag) {
		this.flag = flag;
	}

	public static long of(Permission... permissions) {
		long bits = 0L;
		for (Permission permission : permissions) {
			bits = permission.add(bits);
		}
		return bits;
	}

	public long add(long bits) {
		return bits | this.flag;
	}

	public long remove(long bits) {
		return bits & ~this.flag;
	}

	public long toggle(long bits) {
		return bits ^ this.flag;
	}

	public boolean hasPermission(long bits) {
		return (bits & this.flag) != 0;
	}
}