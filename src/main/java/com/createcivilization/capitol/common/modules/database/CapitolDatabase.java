package com.createcivilization.capitol.common.modules.database;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.compat.sable.data.ClaimedSubLevel;
import com.createcivilization.capitol.common.data.*;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CapitolDatabase extends Database {

	private final ConcurrentHashMap<String, Map<Long, Optional<Team>>> chunkOwnerCache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<UUID, Optional<Team>> subLevelOwnerCache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<UUID, Long> teamPermissionsCache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<UUID, Map<UUID, Long>> playerPermCache = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<UUID, Map<UUID, Optional<Long>>> individualPermCache = new ConcurrentHashMap<>();

	public void clearCache() {
		chunkOwnerCache.clear();
		subLevelOwnerCache.clear();
		teamPermissionsCache.clear();
		playerPermCache.clear();
		individualPermCache.clear();
	}

	/**
	 * Pre-loads chunk ownership from the database into memory.
	 * Call after {@link DatabaseManager#init} on world load.
	 */
	public void warmCache() {
		clearCache();
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT chunks.dimension, chunks.chunk_x, chunks.chunk_z, " +
				"teams.id, teams.name, teams.color, teams.tag, teams.current_claims, " +
				"teams.max_claims, teams.team_permissions, teams.description, teams.created_at " +
				"FROM chunks JOIN teams ON teams.id = chunks.team_id")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String dim = rs.getString("dimension");
					long key = ChunkPos.asLong(rs.getInt("chunk_x"), rs.getInt("chunk_z"));
					chunkOwnerCache.computeIfAbsent(dim, k -> new HashMap<>())
						.put(key, Optional.of(Team.fromResultSet(rs)));
				}
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error warming chunk cache", e);
		}
	}


	public Connection getConnection() {
		return DatabaseManager.getConnection();
	}


	public void setTeamPermissions(Team team, long permissions) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"UPDATE teams SET team_permissions = ? WHERE id = ?")) {
			ps.setLong(1, permissions);
			ps.setString(2, team.getId().toString());
			ps.execute();
			teamPermissionsCache.put(team.getId(), permissions);
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error setting team " + team.getId() + " team permission.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Inserts a new role into the {@code team_roles} table.
	 *
	 * @param team        the team this role belongs to
	 * @param name        the role name (e.g. "owner", "default")
	 * @param permissions the bitfield of {@link Permission} flags
	 */
	public void addRole(Team team, String name, long permissions) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"INSERT INTO team_roles (team_id, name, permissions) VALUES (?, ?, ?)")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, name);
			ps.setLong(3, permissions);
			ps.execute();
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT * FROM team_roles WHERE id = ?")) {
			ps.setInt(1, roleId);
			try (ResultSet rs = ps.executeQuery()) {
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT * FROM team_roles WHERE team_id = ? AND name = ?")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, roleName);
			try (ResultSet rs = ps.executeQuery()) {
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT * FROM team_roles WHERE team_id = ?")) {
			ps.setString(1, team.getId().toString());
			try (ResultSet rs = ps.executeQuery()) {
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
	 * Convenience method that returns the "member" role for a team.
	 *
	 * @param team the team to query
	 * @return the default {@link TeamRole}, or {@code null} if not found
	 */
	public TeamRole getMemberRole(Team team) {
		return getRoleByName(team, TeamRole.MEMBER_ROLE_NAME);
	}

	/**
	 * Updates the permission bitfield for a role identified by team and role name.
	 *
	 * @param team        the team the role belongs to
	 * @param roleName    the name of the role to update
	 * @param permissions the new permission bitfield
	 */
	public void updateRolePermissions(Team team, String roleName, long permissions) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"UPDATE team_roles SET permissions = ? WHERE team_id = ? AND name = ?")) {
			ps.setLong(1, permissions);
			ps.setString(2, team.getId().toString());
			ps.setString(3, roleName);
			ps.execute();
			// All members with this role have stale cached permissions
			playerPermCache.remove(team.getId());
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating role permissions in database.", e);
			throw new RuntimeException(e);
		}
	}

	public void updateRoleName(Team team, String roleName, String newName) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"UPDATE team_roles SET name = ? WHERE team_id = ? AND name = ?")) {
			ps.setString(1, newName);
			ps.setString(2, team.getId().toString());
			ps.setString(3, roleName);
			ps.execute();
		} catch (SQLException e) {
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"DELETE FROM team_roles WHERE team_id = ? AND name = ?")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, roleName);
			ps.execute();
			playerPermCache.remove(team.getId());
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
		String dim = level.dimension().location().toString();
		Map<Long, Optional<Team>> dimCache = chunkOwnerCache.get(dim);
		if (dimCache != null) {
			Optional<Team> cached = dimCache.get(chunkPos.toLong());
			if (cached != null) return cached.isPresent();
		}
		return getChunkOwner(chunkPos, level) != null;
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
		String dim = level.dimension().location().toString();
		Map<Long, Optional<Team>> dimCache = chunkOwnerCache.get(dim);
		if (dimCache != null) {
			Optional<Team> cached = dimCache.get(chunkPos.toLong());
			if (cached != null) return cached.orElse(null);
		}
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.current_claims, teams.max_claims, teams.team_permissions, teams.description, teams.created_at " +
				"FROM chunks " +
				"JOIN teams ON teams.id = chunks.team_id " +
				"WHERE chunks.dimension = ? AND chunks.chunk_x = ? AND chunks.chunk_z = ?")) {
			ps.setString(1, dim);
			ps.setInt(2, chunkPos.x);
			ps.setInt(3, chunkPos.z);
			try (ResultSet rs = ps.executeQuery()) {
				Team team = rs.next() ? Team.fromResultSet(rs) : null;
				chunkOwnerCache.computeIfAbsent(dim, k -> new HashMap<>())
					.put(chunkPos.toLong(), Optional.ofNullable(team));
				return team;
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
	 * @param name the team to persist
	 */
	public boolean teamNameExists(String name) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT 1 FROM teams WHERE name = ?")) {
			ps.setString(1, name);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while checking team name existence in database.", e);
			throw new RuntimeException(e);
		}
	}

	public void addTeam(Team team) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"INSERT INTO teams (id, name, tag, current_claims, description, color, created_at, team_permissions) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, team.getName());
			ps.setString(3, team.getTag());
			ps.setInt(4, 0);
			ps.setString(5, team.getDescription());
			ps.setInt(6, team.getColor().getRGB());
			ps.setLong(7, Instant.now().toEpochMilli());
			ps.setLong(8, team.getTeamPermissions());
			ps.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while inserting team into database.", e);
			throw new RuntimeException(e);
		}

		addRole(team, TeamRole.OWNER_ROLE_NAME, TeamRole.ownerPermissions());
		addRole(team, TeamRole.MEMBER_ROLE_NAME, TeamRole.memberPermissions());
		addRole(team, TeamRole.DEFAULT_ROLE_NAME, TeamRole.defaultPermissions());
	}

	/**
	 * Deletes a team from the {@code teams} table. Cascade deletes will remove
	 * all associated members, roles, and claimed chunks.
	 *
	 * @param team the team to remove
	 */
	public void removeTeam(Team team) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"DELETE FROM teams WHERE id = ?")) {
			ps.setString(1, team.getId().toString());
			ps.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting team from database.", e);
			throw new RuntimeException(e);
		}
		UUID teamId = team.getId();
		chunkOwnerCache.values().forEach(dimCache ->
			dimCache.entrySet().removeIf(e -> e.getValue().isPresent() && e.getValue().get().getId().equals(teamId)));
		subLevelOwnerCache.entrySet().removeIf(e -> e.getValue().isPresent() && e.getValue().get().getId().equals(teamId));
		teamPermissionsCache.remove(teamId);
		playerPermCache.remove(teamId);
		individualPermCache.remove(teamId);
	}

	/**
	 * Retrieves a team by its UUID.
	 *
	 * @param uuid the team's UUID
	 * @return the {@link Team}, or {@code null} if not found
	 */
	public Team getTeam(UUID uuid) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT * FROM teams WHERE id = ?")) {
			ps.setString(1, uuid.toString());
			try (ResultSet rs = ps.executeQuery()) {
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
		return getPlayerTeam(player.getUUID());
	}

	/**
	 * Finds the team a player belongs to by UUID.
	 *
	 * @param playerUUID the player's UUID
	 * @return the player's {@link Team}, or {@code null} if they are not in any team
	 */
	public Team getPlayerTeam(UUID playerUUID) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.current_claims, teams.max_claims, teams.team_permissions, teams.description, teams.created_at " +
				"FROM team_members " +
				"JOIN teams ON teams.id = team_members.team_id " +
				"WHERE team_members.player_uuid = ?")) {
			ps.setString(1, playerUUID.toString());
			try (ResultSet rs = ps.executeQuery()) {
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"INSERT INTO team_members (team_id, player_uuid, role_id) VALUES (?, ?, ?)")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, player.getUUID().toString());
			ps.setInt(3, role.id());
			ps.execute();
			playerPermCache.computeIfAbsent(team.getId(), k -> new HashMap<>())
				.put(player.getUUID(), role.permissions());
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
		removePlayerFromTeam(player.getUUID(), team);
	}

	/**
	 * Removes a player from a team by player UUID.
	 *
	 * @param playerUUID the UUID of the player to remove
	 * @param team       the team to remove them from
	 */
	public void removePlayerFromTeam(UUID playerUUID, Team team) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"DELETE FROM team_members WHERE team_id = ? AND player_uuid = ?")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, playerUUID.toString());
			ps.execute();
			Map<UUID, Long> teamCache = playerPermCache.get(team.getId());
			if (teamCache != null) teamCache.remove(playerUUID);
			Map<UUID, Optional<Long>> indivCache = individualPermCache.get(team.getId());
			if (indivCache != null) indivCache.remove(playerUUID);
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"UPDATE team_members SET role_id = ? WHERE team_id = ? AND player_uuid = ?")) {
			ps.setInt(1, newRole.id());
			ps.setString(2, team.getId().toString());
			ps.setString(3, playerUUID.toString());
			ps.execute();
			playerPermCache.computeIfAbsent(team.getId(), k -> new HashMap<>())
				.put(playerUUID, newRole.permissions());
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT team_roles.* FROM team_members " +
				"JOIN team_roles ON team_roles.id = team_members.role_id " +
				"WHERE team_members.team_id = ? AND team_members.player_uuid = ?")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, player.getUUID().toString());
			try (ResultSet rs = ps.executeQuery()) {
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
		return isPlayerInTeam(player.getUUID(), team);
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
		} catch (SQLException e) {
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT team_members.*, team_roles.name AS role_name " +
				"FROM team_members " +
				"JOIN team_roles ON team_roles.id = team_members.role_id " +
				"WHERE team_members.team_id = ?")) {
			ps.setString(1, team.getId().toString());
			try (ResultSet rs = ps.executeQuery()) {
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
		return getPlayerPermission(player.getUUID(), team);
	}

	public long getPlayerPermission(UUID playerUUID, Team team) {
		Map<UUID, Long> teamCache = playerPermCache.get(team.getId());
		if (teamCache != null) {
			Long cached = teamCache.get(playerUUID);
			if (cached != null) return cached;
		}
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT team_roles.permissions FROM team_members " +
				"JOIN team_roles ON team_roles.id = team_members.role_id " +
				"WHERE team_members.team_id = ? AND team_members.player_uuid = ?")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, playerUUID.toString());
			try (ResultSet rs = ps.executeQuery()) {
				long perms;
				if (rs.next()) {
					perms = rs.getLong("permissions");
				} else {
					TeamRole role = getRoleByName(team, "default");
					perms = role.permissions();
				}
				playerPermCache.computeIfAbsent(team.getId(), k -> new HashMap<>())
					.put(playerUUID, perms);
				return perms;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player permissions from database.", e);
			throw new RuntimeException(e);
		}
	}

	public Long getIndividualPermissions(Player player, Team team) {
		return getIndividualPermissions(player.getUUID(), team);
	}

	public Long getIndividualPermissions(UUID playerUUID, Team team) {
		Map<UUID, Optional<Long>> teamCache = individualPermCache.get(team.getId());
		if (teamCache != null) {
			Optional<Long> cached = teamCache.get(playerUUID);
			if (cached != null) return cached.orElse(null);
		}
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT permissions FROM player_permissions WHERE team_id = ? AND player_uuid = ?")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, playerUUID.toString());
			try (ResultSet rs = ps.executeQuery()) {
				Long perms = rs.next() ? rs.getLong("permissions") : null;
				individualPermCache.computeIfAbsent(team.getId(), k -> new HashMap<>())
					.put(playerUUID, Optional.ofNullable(perms));
				return perms;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting individual permissions from database.", e);
			throw new RuntimeException(e);
		}
	}

	public void setIndividualPermissions(Player player, Team team, long permissions) {
		setIndividualPermissions(player.getUUID(), team, permissions);
	}

	public void setIndividualPermissions(UUID playerUUID, Team team, long permissions) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"INSERT INTO player_permissions (team_id, player_uuid, permissions) VALUES (?, ?, ?) " +
				"ON CONFLICT(team_id, player_uuid) DO UPDATE SET permissions = excluded.permissions")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, playerUUID.toString());
			ps.setLong(3, permissions);
			ps.execute();
			individualPermCache.computeIfAbsent(team.getId(), k -> new HashMap<>())
				.put(playerUUID, Optional.of(permissions));
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while setting individual permissions in database.", e);
			throw new RuntimeException(e);
		}
	}

	public void removeIndividualPermissions(Player player, Team team) {
		removeIndividualPermissions(player.getUUID(), team);
	}

	public void removeIndividualPermissions(UUID playerUUID, Team team) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"DELETE FROM player_permissions WHERE team_id = ? AND player_uuid = ?")) {
			ps.setString(1, team.getId().toString());
			ps.setString(2, playerUUID.toString());
			ps.execute();
			Map<UUID, Optional<Long>> teamCache = individualPermCache.get(team.getId());
			if (teamCache != null) teamCache.put(playerUUID, Optional.empty());
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while removing individual permissions in database.", e);
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
		String dim = level.dimension().location().toString();
		try (PreparedStatement ps = getConnection().prepareStatement(
			"INSERT INTO chunks (dimension, chunk_x, chunk_z, team_id, force_loaded) VALUES (?, ?, ?, ?, ?)")) {
			ps.setString(1, dim);
			ps.setInt(2, chunkPos.x);
			ps.setInt(3, chunkPos.z);
			ps.setString(4, team.getId().toString());
			ps.setBoolean(5, false);
			ps.execute();
			chunkOwnerCache.computeIfAbsent(dim, k -> new HashMap<>())
				.put(chunkPos.toLong(), Optional.of(team));
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
		String dim = level.dimension().location().toString();
		try (PreparedStatement ps = getConnection().prepareStatement(
			"DELETE FROM chunks WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?")) {
			ps.setString(1, dim);
			ps.setInt(2, chunkPos.x);
			ps.setInt(3, chunkPos.z);
			ps.execute();
			Map<Long, Optional<Team>> dimCache = chunkOwnerCache.get(dim);
			if (dimCache != null) dimCache.put(chunkPos.toLong(), Optional.empty());
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT * FROM chunks WHERE team_id = ?")) {
			ps.setString(1, team.getId().toString());
			try (ResultSet rs = ps.executeQuery()) {
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
		try (PreparedStatement ps = getConnection().prepareStatement(
			"DELETE FROM chunks WHERE team_id = ?")) {
			ps.setString(1, team.getId().toString());
			ps.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting all chunks for team from database.", e);
			throw new RuntimeException(e);
		}
		UUID teamId = team.getId();
		chunkOwnerCache.values().forEach(dimCache ->
			dimCache.entrySet().removeIf(e -> e.getValue().isPresent() && e.getValue().get().getId().equals(teamId)));
		resetCurrentClaims(team);
	}

	public List<ClaimedChunk> getAllForceloadedChunks() {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT * FROM chunks WHERE force_loaded = ?")) {
			ps.setBoolean(1, true);
			try (ResultSet rs = ps.executeQuery()) {
				List<ClaimedChunk> chunks = new ArrayList<>();
				while (rs.next()) chunks.add(ClaimedChunk.fromResultSet(rs));
				return chunks;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting force loaded chunks from database.", e);
			throw new RuntimeException(e);
		}
	}

	public boolean isChunkForceloaded(ChunkPos chunkPos, Level level) {
		String dim = level.dimension().location().toString();
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT force_loaded FROM chunks WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?")) {
			ps.setString(1, dim);
			ps.setInt(2, chunkPos.x);
			ps.setInt(3, chunkPos.z);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getBoolean("force_loaded");
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while checking chunk forceload state from database.", e);
			throw new RuntimeException(e);
		}
	}

	public boolean toggleChunkForceload(Team team, ChunkPos chunkPos, Level level) {
		String dim = level.dimension().location().toString();
		boolean current;
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT force_loaded FROM chunks WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?")) {
			ps.setString(1, dim);
			ps.setInt(2, chunkPos.x);
			ps.setInt(3, chunkPos.z);
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) return false;
				current = rs.getBoolean("force_loaded");
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while reading chunk forceload state from database.", e);
			throw new RuntimeException(e);
		}
		boolean newState = !current;
		try (PreparedStatement ps = getConnection().prepareStatement(
			"UPDATE chunks SET force_loaded = ? WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?")) {
			ps.setBoolean(1, newState);
			ps.setString(2, dim);
			ps.setInt(3, chunkPos.x);
			ps.setInt(4, chunkPos.z);
			ps.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating chunk forceload state in database.", e);
			throw new RuntimeException(e);
		}
		return newState;
	}

	public int getTeamForceloadedCount(Team team) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT COUNT(*) FROM chunks WHERE team_id = ? AND force_loaded = 1")) {
			ps.setString(1, team.getId().toString());
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while counting forceloaded chunks for team from database.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Checks whether a specific protection is enabled for a team
	 * using the {@code team_permissions} bitfield.
	 */
	public boolean isProtectionEnabled(Team team, TeamProtection protection) {
		Long cached = teamPermissionsCache.get(team.getId());
		if (cached != null) return protection.hasProtection(cached);
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT team_permissions FROM teams WHERE id = ?")) {
			ps.setString(1, team.getId().toString());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long bits = rs.getLong("team_permissions");
					teamPermissionsCache.put(team.getId(), bits);
					return protection.hasProtection(bits);
				}
				return protection.getConfigDefault();
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while checking team protection", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Toggles a protection bit in the {@code team_permissions} bitfield.
	 * Returns the new enabled state.
	 */
	public boolean toggleProtection(Team team, TeamProtection protection) {
		boolean newState;
		try (PreparedStatement select = getConnection().prepareStatement(
			"SELECT team_permissions FROM teams WHERE id = ?")) {
			select.setString(1, team.getId().toString());
			try (ResultSet rs = select.executeQuery()) {
				if (!rs.next()) return protection.getConfigDefault();
				long bits = rs.getLong("team_permissions");
				bits = protection.toggle(bits);
				newState = protection.hasProtection(bits);

				try (PreparedStatement update = getConnection().prepareStatement(
					"UPDATE teams SET team_permissions = ? WHERE id = ?")) {
					update.setLong(1, bits);
					update.setString(2, team.getId().toString());
					update.execute();
				}
				teamPermissionsCache.put(team.getId(), bits);
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while toggling team protection", e);
			throw new RuntimeException(e);
		}
		return newState;
	}

	/**
	 * Retrieves a claimed chunk record by position and dimension.
	 *
	 * @param chunkPos the chunk position
	 * @param level    the dimension/level the chunk is in
	 * @return the {@link ClaimedChunk}, or {@code null} if unclaimed
	 */
	public ClaimedChunk getChunk(ChunkPos chunkPos, Level level) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT * FROM chunks WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?")) {
			ps.setString(1, level.dimension().location().toString());
			ps.setInt(2, chunkPos.x);
			ps.setInt(3, chunkPos.z);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return ClaimedChunk.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting chunk from database.", e);
			throw new RuntimeException(e);
		}
	}

	private void updateCurrentClaims(Team team, int delta) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"UPDATE teams SET current_claims = current_claims + ? WHERE id = ?")) {
			ps.setInt(1, delta);
			ps.setString(2, team.getId().toString());
			ps.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating current_claims for team.", e);
			throw new RuntimeException(e);
		}
	}

	private void resetCurrentClaims(Team team) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"UPDATE teams SET current_claims = 0 WHERE id = ?")) {
			ps.setString(1, team.getId().toString());
			ps.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while resetting current_claims for team.", e);
			throw new RuntimeException(e);
		}
	}


	//Sable Stuff

	public ClaimedSubLevel getSubLevel(UUID id) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT * FROM sub_levels WHERE id = ?")) {
			ps.setString(1, id.toString());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return ClaimedSubLevel.fromResultSet(rs);
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting sub level from database.", e);
			throw new RuntimeException(e);
		}
	}

	public void claimSubLevel(UUID id, Team team) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"INSERT INTO sub_levels (id, team_id) VALUES (?,?)")) {
			ps.setString(1, id.toString());
			ps.setString(2, team.getId().toString());
			ps.execute();
			subLevelOwnerCache.put(id, Optional.of(team));
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while adding SubLevel to database.", e);
			throw new RuntimeException(e);
		}
	}

	public void removeSubLevel(UUID id) {
		try (PreparedStatement ps = getConnection().prepareStatement(
			"DELETE FROM sub_levels WHERE id = ?")) {
			ps.setString(1, id.toString());
			ps.execute();
			subLevelOwnerCache.put(id, Optional.empty());
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting SubLevel from database.", e);
			throw new RuntimeException(e);
		}
	}

	public Team getSubLevelOwner(UUID id) {
		Optional<Team> cached = subLevelOwnerCache.get(id);
		if (cached != null) return cached.orElse(null);
		try (PreparedStatement ps = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.current_claims, teams.max_claims, teams.team_permissions, teams.description, teams.created_at " +
				"FROM sub_levels " +
				"JOIN teams ON teams.id = sub_levels.team_id " +
				"WHERE sub_levels.id = ?")) {
			ps.setString(1, id.toString());
			try (ResultSet rs = ps.executeQuery()) {
				Team team = rs.next() ? Team.fromResultSet(rs) : null;
				subLevelOwnerCache.put(id, Optional.ofNullable(team));
				return team;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting sub_level owner", e);
			throw new RuntimeException(e);
		}
	}
}