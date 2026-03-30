package com.createcivilization.capitol.common.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record TeamRole(int id, UUID teamId, String name, int permissions) {

	public static final String OWNER_ROLE_NAME = "owner";
	public static final String DEFAULT_ROLE_NAME = "default";

	public boolean isOwner() {
		return OWNER_ROLE_NAME.equals(name);
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
			rs.getInt("permissions")
		);
	}

	public static int ownerPermissions() {
		return Permission.of(Permission.values());
	}

	public static int defaultPermissions() {
		return Permission.of();
	}
}
