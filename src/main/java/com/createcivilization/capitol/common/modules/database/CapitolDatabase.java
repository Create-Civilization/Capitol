package com.createcivilization.capitol.common.modules.database;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.ClaimedChunk;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamMember;
import com.createcivilization.capitol.common.data.TeamRole;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SQLite-backed implementation of {@link Database} that manages all persistent
 * state for teams, members, roles, chunks, and permissions.
 *
 * <p>All methods obtain a JDBC {@link Connection} via {@link DatabaseManager#getConnection()}
 * and use prepared statements with try-with-resources for safe resource cleanup.
 * SQL errors are logged and re-thrown as {@link RuntimeException}.</p>
 *
 * <p>The underlying schema uses cascade deletes — removing a team automatically
 * removes its members, roles, and claimed chunks.</p>
 */
public class CapitolDatabase extends Database {

	/**
	 * Returns the shared JDBC connection from {@link DatabaseManager}.
	 *
	 * @return the active database connection
	 */
	public Connection getConnection(){
		return DatabaseManager.getConnection();
	}

	/**
	 * Inserts a new role into the {@code team_roles} table.
	 *
	 * @param team        the team this role belongs to
	 * @param name        the role name (e.g. "owner", "default")
	 * @param permissions the bitfield of {@link com.createcivilization.capitol.common.data.Permission} flags
	 */
	public void addRole(Team team, String name, long permissions) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"INSERT INTO team_roles (team_id, name, permissions) VALUES (?, ?, ?)")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, name);
			preparedStatement.setLong(3, permissions);
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while adding role to database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves a role by its auto-incremented database ID.
	 *
	 * @param roleId the role's primary key
	 * @return the {@link TeamRole}, or {@code null} if not found
	 */
	public TeamRole getRole(int roleId) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM team_roles WHERE id = ?")) {
			preparedStatement.setInt(1, roleId);
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return TeamRole.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting role from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves a role by team and role name.
	 *
	 * @param team     the team to search in
	 * @param roleName the role name to look up (e.g. "owner")
	 * @return the matching {@link TeamRole}, or {@code null} if not found
	 */
	public TeamRole getRoleByName(Team team, String roleName) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT * FROM team_roles WHERE team_id = ? AND name = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, roleName);
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return TeamRole.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting role by name from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns all roles belonging to a team.
	 *
	 * @param team the team to query
	 * @return list of {@link TeamRole}s (may be empty)
	 */
	public List<TeamRole> getTeamRoles(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM team_roles WHERE team_id = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				List<TeamRole> roles = new ArrayList<>();
				while (rs.next()) roles.add(TeamRole.fromResultSet(rs));
				return roles;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting team roles from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Convenience method that returns the "default" role for a team.
	 *
	 * @param team the team to query
	 * @return the default {@link TeamRole}, or {@code null} if not found
	 */
	public TeamRole getDefaultRole(Team team) {
		return getRoleByName(team, TeamRole.DEFAULT_ROLE_NAME);
	}

	/**
	 * Updates the permission bitfield for a role identified by team and role name.
	 *
	 * @param team        the team the role belongs to
	 * @param roleName    the name of the role to update
	 * @param permissions the new permission bitfield
	 */
	public void updateRolePermissions(Team team, String roleName, long permissions) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"UPDATE team_roles SET permissions = ? WHERE team_id = ? AND name = ?")) {
			preparedStatement.setLong(1, permissions);
			preparedStatement.setString(2, team.getId().toString());
			preparedStatement.setString(3, roleName);
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating role permissions in database.", e);
			throw new RuntimeException(e);
		}
	}

	public void updateRoleName(Team team, String roleName, String newName){
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
		"UPDATE team_roles SET name = ? WHERE team_id = ? AND name = ?")) {
			preparedStatement.setString(1, newName);
			preparedStatement.setString(2, team.getId().toString());
			preparedStatement.setString(3, roleName);
			preparedStatement.execute();
		} catch (SQLException e){
			Capitol.LOGGER.error("Error while updating role name in database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Deletes a role by team and role name.
	 *
	 * @param team     the team the role belongs to
	 * @param roleName the name of the role to delete
	 */
	public void deleteRole(Team team, String roleName) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"DELETE FROM team_roles WHERE team_id = ? AND name = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, roleName);
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting role from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Returns {@code true} if any team owns the chunk at the given position and dimension.</p>
	 */
	@Override
	public boolean hasChunkAt(ChunkPos chunkPos, Level level) {
		return getChunkOwner(chunkPos, level) != null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Joins {@code chunks}, {@code team_members}, and {@code team_roles} to resolve the
	 * player's permission bitfield within the chunk's owning team. Returns {@code 0} if the
	 * player is not a member of the team that owns this chunk.</p>
	 *
	 * @param player   the player to check permissions for
	 * @param chunkPos the chunk position
	 * @param level    the dimension/level the chunk is in
	 * @return the permission bitfield, or {@code 0} if the player has no permissions here
	 */
	@Override
	public long getPermissionInChunk(Player player, ChunkPos chunkPos, Level level) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT team_roles.permissions " +
			"FROM chunks " +
			"JOIN team_members ON team_members.team_id = chunks.team_id AND team_members.player_uuid = ? " +
			"JOIN team_roles ON team_roles.id = team_members.role_id " +
			"WHERE chunks.dimension = ? AND chunks.chunk_x = ? AND chunks.chunk_z = ?")) {
			preparedStatement.setString(1, player.getUUID().toString());
			preparedStatement.setString(2, level.dimension().location().toString());
			preparedStatement.setInt(3, chunkPos.x);
			preparedStatement.setInt(4, chunkPos.z);
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return rs.getLong("permissions");
				return 0L;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player permissions in chunk", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Joins {@code chunks} and {@code teams} to return the owning team for a chunk.</p>
	 *
	 * @param chunkPos the chunk position
	 * @param level    the dimension/level the chunk is in
	 * @return the owning {@link Team}, or {@code null} if the chunk is unclaimed
	 */
	@Override
	public Team getChunkOwner(ChunkPos chunkPos, Level level) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.current_claims, teams.max_claims, teams.description, teams.created_at " +
				"FROM chunks " +
				"JOIN teams ON teams.id = chunks.team_id " +
				"WHERE chunks.dimension = ? AND chunks.chunk_x = ? AND chunks.chunk_z = ?")) {
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return Team.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting chunk owner", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Inserts a new team into the {@code teams} table and creates the default
	 * "owner" and "default" roles for it.
	 *
	 * @param team the team to persist
	 */
	public void addTeam(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO teams (id, name, tag, current_claims, description, color, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, team.getName());
			preparedStatement.setString(3, team.getTag());
			preparedStatement.setInt(4, 0);
			preparedStatement.setString(5, team.getDescription());
			preparedStatement.setInt(6, team.getColor().getRGB());
			preparedStatement.setLong(7, Instant.now().toEpochMilli());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while inserting team into database.", e);
			throw new RuntimeException(e);
		}

		addRole(team, TeamRole.OWNER_ROLE_NAME, TeamRole.ownerPermissions());
		addRole(team, TeamRole.DEFAULT_ROLE_NAME, TeamRole.defaultPermissions());
	}

	/**
	 * Deletes a team from the {@code teams} table. Cascade deletes will remove
	 * all associated members, roles, and claimed chunks.
	 *
	 * @param team the team to remove
	 */
	public void removeTeam(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"DELETE FROM teams WHERE id = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting team from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves a team by its UUID.
	 *
	 * @param uuid the team's UUID
	 * @return the {@link Team}, or {@code null} if not found
	 */
	public Team getTeam(UUID uuid) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT * FROM teams WHERE id = ?")) {
			preparedStatement.setString(1, uuid.toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return Team.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting team from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Finds the team a player belongs to by joining {@code team_members} and {@code teams}.
	 *
	 * @param player the player entity
	 * @return the player's {@link Team}, or {@code null} if they are not in any team
	 */
	public Team getPlayerTeam(Player player) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.current_claims, teams.max_claims, teams.description, teams.created_at " +
				"FROM team_members " +
				"JOIN teams ON teams.id = team_members.team_id " +
				"WHERE team_members.player_uuid = ?")) {
			preparedStatement.setString(1, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return Team.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player's team from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Finds the team a player belongs to by UUID.
	 *
	 * @param playerUUID the player's UUID
	 * @return the player's {@link Team}, or {@code null} if they are not in any team
	 */
	public Team getPlayerTeam(UUID playerUUID) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.current_claims, teams.max_claims, teams.description, teams.created_at " +
				"FROM team_members " +
				"JOIN teams ON teams.id = team_members.team_id " +
				"WHERE team_members.player_uuid = ?")) {
			preparedStatement.setString(1, playerUUID.toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return Team.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player's team from database.", e);
			throw new RuntimeException(e);
		}
	}



	/**
	 * Adds a player to a team with the specified role.
	 *
	 * @param player the player to add
	 * @param team   the team to join
	 * @param role   the role to assign
	 */
	public void addPlayerToTeam(Player player, Team team, TeamRole role) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"INSERT INTO team_members (team_id, player_uuid, role_id) VALUES (?, ?, ?)")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			preparedStatement.setInt(3, role.id());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while adding player to team in database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes a player from a team.
	 *
	 * @param player the player to remove
	 * @param team   the team to remove them from
	 */
	public void removePlayerFromTeam(Player player, Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"DELETE FROM team_members WHERE team_id = ? AND player_uuid = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while removing player from team in database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes a player from a team by player UUID.
	 *
	 * @param playerUUID the UUID of the player to remove
	 * @param team       the team to remove them from
	 */
	public void removePlayerFromTeam(UUID playerUUID, Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"DELETE FROM team_members WHERE team_id = ? AND player_uuid = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, playerUUID.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while removing player from team in database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Changes a player's role within a team.
	 *
	 * @param player  the player whose role is being changed
	 * @param team    the team the player belongs to
	 * @param newRole the new role to assign
	 */
	public void updatePlayerRole(Player player, Team team, TeamRole newRole) {
		updatePlayerRole(player.getUUID(), team, newRole);
	}

	public void updatePlayerRole(UUID playerUUID, Team team, TeamRole newRole) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"UPDATE team_members SET role_id = ? WHERE team_id = ? AND player_uuid = ?")) {
			preparedStatement.setInt(1, newRole.id());
			preparedStatement.setString(2, team.getId().toString());
			preparedStatement.setString(3, playerUUID.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating player role in database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the role a player holds within a specific team.
	 *
	 * @param player the player to look up
	 * @param team   the team to check membership in
	 * @return the player's {@link TeamRole}, or {@code null} if they are not a member
	 */
	public TeamRole getPlayerRole(Player player, Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT team_roles.* FROM team_members " +
				"JOIN team_roles ON team_roles.id = team_members.role_id " +
				"WHERE team_members.team_id = ? AND team_members.player_uuid = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return TeamRole.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player role from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks whether a player is a member of a team.
	 *
	 * @param player the player to check
	 * @param team   the team to check against
	 * @return {@code true} if the player is a member
	 */
	public boolean isPlayerInTeam(Player player, Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT 1 FROM team_members WHERE team_id = ? AND player_uuid = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while checking player membership in database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks whether a player is a member of a team by player UUID.
	 *
	 * @param playerUUID the player's UUID
	 * @param team       the team to check against
	 * @return {@code true} if the player is a member
	 */
	public boolean isPlayerInTeam(UUID playerUUID, Team team){
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT 1 FROM team_members WHERE team_id = ? AND player_uuid = ?"
		)) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, playerUUID.toString());
			try (ResultSet rs = preparedStatement.executeQuery()){
				return rs.next();
			}
		} catch (SQLException e){
			Capitol.LOGGER.error("Error while checking player membership in database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns all members of a team, including their role names.
	 *
	 * @param team the team to query
	 * @return list of {@link TeamMember}s (may be empty)
	 */
	public List<TeamMember> getTeamMembers(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT team_members.*, team_roles.name AS role_name " +
				"FROM team_members " +
				"JOIN team_roles ON team_roles.id = team_members.role_id " +
				"WHERE team_members.team_id = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				List<TeamMember> members = new ArrayList<>();
				while (rs.next()) members.add(TeamMember.fromResultSet(rs));
				return members;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting team members in database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the permission bitfield for a player within a specific team.
	 *
	 * @param player the player to look up
	 * @param team   the team to check permissions in
	 * @return the permission bitfield, or {@code 0} if the player is not a member
	 */
	public long getPlayerPermission(Player player, Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT team_roles.permissions FROM team_members " +
				"JOIN team_roles ON team_roles.id = team_members.role_id " +
				"WHERE team_members.team_id = ? AND team_members.player_uuid = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return rs.getLong("permissions");
				TeamRole role = getRoleByName(team, "default");
				return role.permissions();
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player permissions from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Claims a chunk for a team by inserting it into the {@code chunks} table
	 * and incrementing the team's {@code current_claims} counter.
	 *
	 * @param team     the team claiming the chunk
	 * @param chunkPos the chunk position to claim
	 * @param level    the dimension/level the chunk is in
	 */
	public void claimChunk(Team team, ChunkPos chunkPos, Level level) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"INSERT INTO chunks (dimension, chunk_x, chunk_z, team_id) VALUES (?, ?, ?, ?)")) {
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			preparedStatement.setString(4, team.getId().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while inserting chunk into database.", e);
			throw new RuntimeException(e);
		}
		updateCurrentClaims(team, 1);
	}

	/**
	 * Unclaims a chunk by removing it from the {@code chunks} table
	 * and decrementing the owning team's {@code current_claims} counter.
	 *
	 * @param team     the team that owns the chunk
	 * @param chunkPos the chunk position to unclaim
	 * @param level    the dimension/level the chunk is in
	 */
	public void unclaimChunk(Team team, ChunkPos chunkPos, Level level) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"DELETE FROM chunks WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?")) {
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting chunk from database.", e);
			throw new RuntimeException(e);
		}
		updateCurrentClaims(team, -1);
	}

	/**
	 * Returns all chunks claimed by a team.
	 *
	 * @param team the team to query
	 * @return list of {@link ClaimedChunk}s (may be empty)
	 */
	public List<ClaimedChunk> getTeamChunks(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT * FROM chunks WHERE team_id = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				List<ClaimedChunk> chunks = new ArrayList<>();
				while (rs.next()) chunks.add(ClaimedChunk.fromResultSet(rs));
				return chunks;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting team chunks from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes all chunk claims for a team and resets its {@code current_claims} to 0.
	 *
	 * @param team the team whose chunks should be unclaimed
	 */
	public void unclaimAllChunks(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"DELETE FROM chunks WHERE team_id = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting all chunks for team from database.", e);
			throw new RuntimeException(e);
		}
		resetCurrentClaims(team);
	}

	/**
	 * Adjusts a team's {@code current_claims} counter by the given delta.
	 *
	 * @param team  the team to update
	 * @param delta the amount to add (positive) or subtract (negative)
	 */
	private void updateCurrentClaims(Team team, int delta) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"UPDATE teams SET current_claims = current_claims + ? WHERE id = ?")) {
			preparedStatement.setInt(1, delta);
			preparedStatement.setString(2, team.getId().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating current_claims for team.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Resets a team's {@code current_claims} counter to 0.
	 *
	 * @param team the team to reset
	 */
	private void resetCurrentClaims(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"UPDATE teams SET current_claims = 0 WHERE id = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while resetting current_claims for team.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves a claimed chunk record by position and dimension.
	 *
	 * @param chunkPos the chunk position
	 * @param level    the dimension/level the chunk is in
	 * @return the {@link ClaimedChunk}, or {@code null} if unclaimed
	 */
	public ClaimedChunk getChunk(ChunkPos chunkPos, Level level) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT * FROM chunks WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?")) {
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return ClaimedChunk.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting chunk from database.", e);
			throw new RuntimeException(e);
		}
	}
}
