package com.createcivilization.capitol.common.data;

public enum Permission {
	CLAIM_CHUNKS        (1 << 0),
	UNCLAIM_CHUNKS      (1 << 1),
	BREAK_BLOCKS        (1 << 2),
	PLACE_BLOCKS        (1 << 3),
	INTERACT_BLOCKS     (1 << 4),   // doors, buttons, levers, etc.
	OPEN_CONTAINERS     (1 << 5),   // chests, barrels, hoppers, furnaces
	USE_REDSTONE        (1 << 6),   // repeaters, comparators, etc.
	INTERACT_ENTITIES   (1 << 7),   // villagers, item frames, armor stands
	KILL_FRIENDLIES     (1 << 8),   // livestock, pets
	KILL_HOSTILES       (1 << 9),
	USE_ITEMS           (1 << 10),  // buckets, flint & steel, etc.
	PICKUP_ITEMS        (1 << 11),
	INVITE_MEMBERS      (1 << 12),
	KICK_MEMBERS        (1 << 13),
	MANAGE_ROLES        (1 << 14),  // create/edit/delete roles
	ASSIGN_ROLES        (1 << 15),  // change a member's role
	MANAGE_TEAM         (1 << 16);  // rename team, disband, etc.


	private final int bit;

	Permission(int bit) {
		this.bit = bit;
	}

	public static int of(Permission... permissions) {
		int bits = 0;
		for (Permission permission : permissions) {
			bits = permission.add(bits);
		}
		return bits;
	}

	public int add(int bits){
		return bits | this.bit;
	}

	public int remove(int bits){
		return bits & ~this.bit;
	}

	public int toggle(int bits){
		return bits ^ this.bit;
	}

	public boolean hasPermission(int bits){
		int hasPerms = bits & this.bit;
		return hasPerms != 0;
	}
}

