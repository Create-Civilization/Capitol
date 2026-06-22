package com.createcivilization.capitol.common.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record TeamRole(int id, UUID teamId, String name, long permissions) {

	public static final String OWNER_ROLE_NAME = "owner";
	public static final String MEMBER_ROLE_NAME = "member";
	public static final String DEFAULT_ROLE_NAME = "default";

	public boolean isOwner() {
		return OWNER_ROLE_NAME.equals(name);
	}

	public boolean isMemberRole() {
		return MEMBER_ROLE_NAME.equals(name);
	}
	public boolean isDefaultRole() {
		return DEFAULT_ROLE_NAME.equals(name);
	}

	public boolean hasPermission(Permission perm) {
		return perm.hasPermission(permissions);
	}

	public static TeamRole fromResultSet(ResultSet rs) throws SQLException {
		return new TeamRole(
			rs.getInt("id"),
			UUID.fromString(rs.getString("team_id")),
			rs.getString("name"),
			rs.getLong("permissions")
		);
	}

	public static long ownerPermissions() {
		return Permission.of(Permission.values());
	}

	public static long memberPermissions() {
		return Permission.of(
			Permission.BREAK_BLOCKS,
			Permission.PLACE_BLOCKS,
			Permission.INTERACT_BLOCKS,
			Permission.OPEN_CONTAINERS,
			Permission.CROP_TRAMPLE,
			Permission.FROST_WALKING,
			Permission.INTERACT_ENTITIES,
			Permission.ATTACK_PASSIVE,
			Permission.ATTACK_HOSTILE,
			Permission.ATTACK_PLAYER,
			Permission.USE_ITEMS,
			Permission.PICKUP_ITEMS,
			Permission.PICKUP_XP,
			Permission.DROP_ITEMS,
			Permission.MOB_LOOT,
			Permission.PLAYER_DEATH_LOOT,
			Permission.INTERACT_REDSTONE,
			Permission.USE_NETHER_PORTALS,
			Permission.CHORUS_FRUIT_TELEPORT
		);
		// return 0b111111111111111111100000000L;
	}
	public static long defaultPermissions() {
		return 0;
	}
}