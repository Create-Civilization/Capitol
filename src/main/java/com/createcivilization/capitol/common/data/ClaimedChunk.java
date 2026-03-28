package com.createcivilization.capitol.common.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public record ClaimedChunk(String dimension, int chunkX, int chunkZ) {

	public static ClaimedChunk fromResultSet(ResultSet rs) throws SQLException {
		return new ClaimedChunk(
			rs.getString("dimension"),
			rs.getInt("chunk_x"),
			rs.getInt("chunk_z")
		);
	}
}