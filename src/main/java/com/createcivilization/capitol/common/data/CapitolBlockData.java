package com.createcivilization.capitol.common.data;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

// one row of the capitol_blocks table: a single placed block
public record CapitolBlockData(long id, UUID teamId, String dimension, int x, int y, int z,
							   boolean capital, @Nullable CapitolTier tier) {

	public BlockPos pos() {
		return new BlockPos(x, y, z);
	}

	public static CapitolBlockData fromResultSet(ResultSet rs) throws SQLException {
		return new CapitolBlockData(
			rs.getLong("id"),
			UUID.fromString(rs.getString("team_id")),
			rs.getString("dimension"),
			rs.getInt("x"),
			rs.getInt("y"),
			rs.getInt("z"),
			rs.getInt("is_capital") == 1,
			CapitolTier.byName(rs.getString("tier"))
		);
	}
}
