package com.createcivilization.capitol.common.data;

public enum Role {
	OWNER("owner", Permission.of(Permission.values())),
	OFFICER("officer", Permission.of(
		Permission.CLAIM_CHUNKS, Permission.UNCLAIM_CHUNKS,
		Permission.BREAK_BLOCKS, Permission.PLACE_BLOCKS,
		Permission.INTERACT_BLOCKS, Permission.OPEN_CONTAINERS,
		Permission.USE_REDSTONE, Permission.INTERACT_ENTITIES,
		Permission.KILL_FRIENDLIES, Permission.KILL_HOSTILES,
		Permission.USE_ITEMS, Permission.PICKUP_ITEMS,
		Permission.INVITE_MEMBERS, Permission.KICK_MEMBERS,
		Permission.ASSIGN_ROLES
	)),
	MEMBER("member", Permission.of(
		Permission.BREAK_BLOCKS, Permission.PLACE_BLOCKS,
		Permission.INTERACT_BLOCKS, Permission.OPEN_CONTAINERS,
		Permission.USE_REDSTONE, Permission.INTERACT_ENTITIES,
		Permission.KILL_FRIENDLIES, Permission.KILL_HOSTILES,
		Permission.USE_ITEMS, Permission.PICKUP_ITEMS
	)),
	DEFAULT("default", Permission.of());


	private final String id;
	private final int defaultPerms;

	Role(String id, int defaultPerms) {
		this.id = id;
		this.defaultPerms = defaultPerms;
	}

	public String getID() {
		return this.id;
	}

	//TODO: Config for default perms.
	public int getDefaultPerms() {
		return this.defaultPerms;
	}

	public static Role fromID(String id) {
		for (Role role : Role.values()) {
			if (role.getID().equals(id)) {
				return role;
			}
		}
		throw new IllegalArgumentException("No such role: " + id);
	}
}
