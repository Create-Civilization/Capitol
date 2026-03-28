package com.createcivilization.capitol.common.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record TeamMember(UUID playerUuid, String role, int permissions) {

	public static TeamMember fromResultSet(ResultSet rs) throws SQLException {
		return new TeamMember(
			UUID.fromString(rs.getString("player_uuid")),
			rs.getString("role"),
			rs.getInt("permissions")
		);
	}
}
