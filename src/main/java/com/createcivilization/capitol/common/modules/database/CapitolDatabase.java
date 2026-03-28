package com.createcivilization.capitol.common.modules.database;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.ClaimedChunk;
import com.createcivilization.capitol.common.data.Role;
import com.createcivilization.capitol.common.data.Team;
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

	@Override
	public boolean hasChunkAt(ChunkPos chunkPos, Level level) {
		return getChunkOwner(chunkPos, level) != null;
	}

	@Override
	public int getPermissionInChunk(Player player, ChunkPos chunkPos, Level level) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT tm.permissions " +
			"FROM chunks " +
			"JOIN team_members tm ON tm.team_id = chunks.team_id " +
			"WHERE chunks.dimension = ? AND chunks.chunk_x = ? AND chunks.chunk_z = ? AND tm.player_uuid = ?")) {
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			preparedStatement.setString(4, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return rs.getInt("permissions");
				return Role.DEFAULT.getDefaultPerms();
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player permissions in chunk", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Team getChunkOwner(ChunkPos chunkPos, Level level) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.created_at " +
				"FROM chunks " +
				"JOIN teams ON teams.id = chunks.team_id " +
				"WHERE chunks.dimension = ? AND chunks.chunk_x = ? AND chunks.chunk_z = ?")) {
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) {
					return Team.fromResultSet(rs);
				}
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting chunk owner", e);
			throw new RuntimeException(e);
		}
	}

	public void addTeam(Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"INSERT INTO teams (id, name, color, created_at) VALUES (?, ?, ?, ?)")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, team.getName());
			preparedStatement.setInt(3, team.getColor().getRGB());
			preparedStatement.setLong(4, Instant.now().toEpochMilli());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while inserting team into database.", e);
			throw new RuntimeException(e);
		}
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
				if (rs.next()) {
					return Team.fromResultSet(rs);
				}
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting team from database.", e);
			throw new RuntimeException(e);
		}
	}

	public Team getPlayerTeam(Player player) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT teams.id, teams.name, teams.color, teams.created_at " +
				"FROM team_members " +
				"JOIN teams ON teams.id = team_members.team_id " +
				"WHERE team_members.player_uuid = ?")) {
			preparedStatement.setString(1, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) {
					return Team.fromResultSet(rs);
				}
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting player's team from database.", e);
			throw new RuntimeException(e);
		}
	}

	public void addPlayerToTeam(Player player, Team team, Role role) {

		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"INSERT INTO team_members (team_id, player_uuid, role, permissions) VALUES (?, ?, ?, ?)")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			preparedStatement.setString(3, role.getID());
			preparedStatement.setInt(4, role.getDefaultPerms());
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

	public void updatePlayerRole(Player player, Team team, Role newRole) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"UPDATE team_members SET role = ?, permissions = ? WHERE team_id = ? AND player_uuid = ?")) {
			preparedStatement.setString(1, newRole.getID());
			preparedStatement.setInt(2, newRole.getDefaultPerms());
			preparedStatement.setString(3, team.getId().toString());
			preparedStatement.setString(4, player.getUUID().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while updating player role in database.", e);
			throw new RuntimeException(e);
		}
	}

	public Role getPlayerRole(Player player, Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT role FROM team_members WHERE team_id = ? AND player_uuid = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) {
					return Role.fromID(rs.getString("role"));
				}
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

	public int getPlayerPermission(Player player, Team team) {
		try (PreparedStatement preparedStatement = getConnection().prepareStatement(
			"SELECT permissions FROM team_members WHERE team_id = ? AND player_uuid = ?")) {
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, player.getUUID().toString());
			try (ResultSet rs = preparedStatement.executeQuery()) {
				if (rs.next()) return rs.getInt("permissions");
				return -1; // player not in team
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
				if (rs.next()) {
					return ClaimedChunk.fromResultSet(rs);
				}
				return null;
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting chunk from database.", e);
			throw new RuntimeException(e);
		}
	}
}