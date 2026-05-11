package com.createcivilization.capitol.common.data;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record TeamRole(int id, UUID teamId, String name, long permissions, Color color) {

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
		Color color = null;
		if (rs.getInt("color") != -1) {
			color = new Color(rs.getInt("color"));
		}

		return new TeamRole(
			rs.getInt("id"),
			UUID.fromString(rs.getString("team_id")),
			rs.getString("name"),
			rs.getLong("permissions"),
			color
		);
	}

	public static long ownerPermissions() {
		return Permission.of(Permission.values());
	}

	public static long defaultPermissions() {
		return Permission.of();
	}
}