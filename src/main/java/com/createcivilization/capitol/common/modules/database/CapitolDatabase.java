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

public class CapitolDatabase extends Database {

	public Connection getConnection(){
		return DatabaseManager.getConnection();
	}

	public void addRole(UUID teamId, String name, int permissions) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"INSERT INTO team_roles (team_id, name, permissions) VALUES (?, ?, ?)")) {
			preparedStatement.setString(1, teamId.toString());
			preparedStatement.setString(2, name);
			preparedStatement.setInt(3, permissions);
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while adding role to database.", e);
			throw new RuntimeException(e);
		}
	}

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

	public TeamRole getRoleByName(UUID teamId, String roleName) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT * FROM team_roles WHERE team_id = ? AND name = ?")) {
			preparedStatement.setString(1, teamId.toString());
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

	public List<TeamRole> getTeamRoles(UUID teamId) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement("SELECT * FROM team_roles WHERE team_id = ?")) {
			preparedStatement.setString(1, teamId.toString());
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

	public TeamRole getDefaultRole(UUID teamId) {
		return getRoleByName(teamId, TeamRole.DEFAULT_ROLE_NAME);
	}

	public void updateRolePermissions(int roleId, int permissions) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement("UPDATE team_roles SET permissions = ? WHERE id = ?")) {
			preparedStatement.setInt(1, permissions);
			preparedStatement.setInt(2, roleId);
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating role permissions in database.", e);
			throw new RuntimeException(e);
		}
	}

	public void deleteRole(int roleId) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"DELETE FROM team_roles WHERE id = ?")) {
			preparedStatement.setInt(1, roleId);
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting role from database.", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasChunkAt(ChunkPos chunkPos, Level level) {
		return getChunkOwner(chunkPos, level) != null;
	}

	@Override
	public int getPermissionInChunk(Player player, ChunkPos chunkPos, Level level) {
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
				if (rs.next()) return rs.getInt("permissions");
				return 0;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player permissions in chunk", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Team getChunkOwner(ChunkPos chunkPos, Level level) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.description, teams.created_at " +
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

	public void addTeam(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement("INSERT INTO teams (id, name, tag, description, color, created_at) VALUES (?, ?, ?, ?, ?, ?)")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, team.getName());
			preparedStatement.setString(3, team.getTag());
			preparedStatement.setString(4, team.getDescription());
			preparedStatement.setInt(5, team.getColor().getRGB());
			preparedStatement.setLong(6, Instant.now().toEpochMilli());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while inserting team into database.", e);
			throw new RuntimeException(e);
		}

		addRole(team.getId(), TeamRole.OWNER_ROLE_NAME, TeamRole.ownerPermissions());
		addRole(team.getId(), TeamRole.DEFAULT_ROLE_NAME, TeamRole.defaultPermissions());
	}

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

	public Team getPlayerTeam(Player player) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.description, teams.created_at " +
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

	public Team getPlayerTeam(UUID playerUUID) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.tag, teams.description, teams.created_at " +
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

	public void updatePlayerRole(Player player, Team team, TeamRole newRole) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"UPDATE team_members SET role_id = ? WHERE team_id = ? AND player_uuid = ?")) {
			preparedStatement.setInt(1, newRole.id());
			preparedStatement.setString(2, team.getId().toString());
			preparedStatement.setString(3, player.getUUID().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating player role in database.", e);
			throw new RuntimeException(e);
		}
	}

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

	public int getPlayerPermission(Player player, Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT team_roles.permissions FROM team_members " +
				"JOIN team_roles ON team_roles.id = team_members.role_id " +
				"WHERE team_members.team_id = ? AND team_members.player_uuid = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return rs.getInt("permissions");
				return 0;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player permissions from database.", e);
			throw new RuntimeException(e);
		}
	}

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
	}

	public void unclaimChunk(ChunkPos chunkPos, Level level) {
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
	}

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

	public void unclaimAllChunks(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"DELETE FROM chunks WHERE team_id = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting all chunks for team from database.", e);
			throw new RuntimeException(e);
		}
	}

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
