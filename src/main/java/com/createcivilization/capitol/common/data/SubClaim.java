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
	int maxX, int maxY, int maxZ,
	UUID ownerUuid,
	long permissions,
	long protections
) {

	public static SubClaim fromResultSet(ResultSet rs) throws SQLException {
		String ownerUuid = rs.getString("owner_uuid");
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
			rs.getInt("max_z"),
			// old rows have no owner (migrated before ownership existed)
			ownerUuid == null || ownerUuid.isEmpty() ? null : UUID.fromString(ownerUuid),
			rs.getLong("permissions"),
			rs.getLong("protections")
		);
	}

	/** Returns true if the given world position falls inside this subclaim volume. */
	public boolean contains(int x, int y, int z) {
		return x >= minX && x <= maxX
			&& y >= minY && y <= maxY
			&& z >= minZ && z <= maxZ;
	}

	// does the sub-claim give members this permission?
	public boolean hasPermission(Permission permission) {
		return permission.hasPermission(this.permissions);
	}

	public SubClaim withPermissions(long newPermissions) {
		return new SubClaim(id, teamId, name, dimension,
			minX, minY, minZ, maxX, maxY, maxZ,
			ownerUuid, newPermissions, protections);
	}

	// is this protection on for the sub-claim?
	public boolean hasProtection(SubClaimProtection protection) {
		return protection.hasProtection(this.protections);
	}

	public SubClaim withProtections(long newProtections) {
		return new SubClaim(id, teamId, name, dimension,
			minX, minY, minZ, maxX, maxY, maxZ,
			ownerUuid, permissions, newProtections);
	}
}