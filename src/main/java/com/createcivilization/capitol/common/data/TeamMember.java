package com.createcivilization.capitol.common.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record TeamMember(UUID teamId, UUID playerUUID, int roleId, String roleName) {

	public static TeamMember fromResultSet(ResultSet rs) throws SQLException {
		return new TeamMember(
			UUID.fromString(rs.getString("team_id")),
			UUID.fromString(rs.getString("player_uuid")),
			rs.getInt("role_id"),
			rs.getString("role_name")
		);
	}
}
