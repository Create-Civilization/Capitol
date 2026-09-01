package com.createcivilization.capitol.common.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record SubClaim(
	UUID id,
	UUID teamId,
	String name,
	String dimension,
	int minX, int minY, int minZ,
	int maxX, int maxY, int maxZ
) {

	public static SubClaim fromResultSet(ResultSet rs) throws SQLException {
		return new SubClaim(
			UUID.fromString(rs.getString("id")),
			UUID.fromString(rs.getString("team_id")),
			rs.getString("name"),
			rs.getString("dimension"),
			rs.getInt("min_x"),
			rs.getInt("min_y"),
			rs.getInt("min_z"),
			rs.getInt("max_x"),
			rs.getInt("max_y"),
			rs.getInt("max_z")
		);
	}

	/** Returns true if the given world position falls inside this subclaim volume. */
	public boolean contains(int x, int y, int z) {
		return x >= minX && x <= maxX
			&& y >= minY && y <= maxY
			&& z >= minZ && z <= maxZ;
	}
}