package com.createcivilization.capitol.common.compat.sable;

import com.createcivilization.capitol.common.data.ClaimedChunk;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public record ClaimedSubLevel(UUID id) {

	public static ClaimedSubLevel fromResultSet(ResultSet rs) throws SQLException {
		return new ClaimedSubLevel(
			UUID.fromString(rs.getString("id"))
		);
	}

}
