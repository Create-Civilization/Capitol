package com.createcivilization.capitol.common.data;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

// one row of the capitol_blocks table: a single placed block
public record CapitolBlockData(long id, UUID teamId, String dimension, int x, int y, int z,
							   boolean capital, @Nullable CapitolTier tier,
							   @Nullable String name, @Nullable UUID mayorUuid) {

	// chunks out the Capital claims on placement (3 = 7x7, its own chunk + 3 each way)
	public static final int CAPITAL_CLAIM_RADIUS = 3;
	// chunks out an extra block takes over existing claims (2 = 5x5)
	public static final int ADDITIONAL_CLAIM_RADIUS = 2;

	public BlockPos pos() {
		return new BlockPos(x, y, z);
	}

	// how far out (in chunks) this block's claim reaches
	public int claimRadius() {
		return capital ? CAPITAL_CLAIM_RADIUS : ADDITIONAL_CLAIM_RADIUS;
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
			CapitolTier.byName(rs.getString("tier")),
			rs.getString("name"),
			rs.getString("mayor_uuid") == null ? null : UUID.fromString(rs.getString("mayor_uuid"))
		);
	}
}