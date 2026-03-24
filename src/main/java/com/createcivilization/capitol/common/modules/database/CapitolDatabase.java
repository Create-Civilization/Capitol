package com.createcivilization.capitol.common.modules.database;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.ClaimedChunk;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;

public class CapitolDatabase extends Database {

	private static final Connection connection = DatabaseManager.getConnection();

	@Override
	public boolean hasChunkAt(ChunkPos chunkPos, Level level) {
		if(getChunkOwner(chunkPos, level) == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean hasPermissionInChunk(Player player, ChunkPos chunkPos, Level level) {
		return false;
	}

	@Override
	public Team getChunkOwner(ChunkPos chunkPos, Level level) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT teams.id, teams.name, teams.created_at " +
					"FROM chunks c " +
					"JOIN teams t ON t.id = c.team_id = t.id " +
					"WHERE c.dimension = ? AND c.chunk_x = ? AND c.chunk_z = ?"
			);
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.next()){
				return Team.fromResultSet(rs);
			}
			return null;
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting chunk owner", e);
			throw new RuntimeException(e);
		}
	}

	public void addTeam(Team team) {
		try{
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO teams (id,name,created_at) VALUES (?,?,?)");
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.setString(2, team.getName());
			preparedStatement.setInt(3, Timestamp.from(Instant.now()).getNanos());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while inserting team into database.", e);
			throw new RuntimeException(e);
		}
	}

	public void removeTeam(Team team) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM teams WHERE id = ?");
			preparedStatement.setString(1, team.getId().toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting team into database.", e);
			throw new RuntimeException(e);
		}
	}

	public Team getTeam(UUID uuid) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM teams WHERE id = ?");
			preparedStatement.setString(1, uuid.toString());
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				return Team.fromResultSet(rs);
			}
			return null;
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting team from database.", e);
			throw new RuntimeException(e);
		}
	}

	public void claimChunk(Team team, ChunkPos chunkPos, Level level) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO chunks (dimension, chunk_x, chunk_z, team_id) VALUES (?,?,?,?)");
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			preparedStatement.setString(4, team.getId().toString());
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while inserting chunk into database.", e);
			throw new RuntimeException(e);
		}
	}

	public void unclaimChunk(ChunkPos chunkPos, Level level) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM chunks WHERE dimension = ? AND chunk_x = ? AND chunk_z = ?");
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while deleting chunk from database.", e);
			throw new RuntimeException(e);
		}
	}

	public ClaimedChunk getChunk(ChunkPos chunkPos, Level level) {
		try {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM chunks where dimension = ? AND chunk_x = ? AND chunk_z = ?");
			preparedStatement.setString(1, level.dimension().location().toString());
			preparedStatement.setInt(2, chunkPos.x);
			preparedStatement.setInt(3, chunkPos.z);
			ResultSet rs = preparedStatement.executeQuery();
			if(rs.next()){
				return ClaimedChunk.fromResultSet(rs);
			}
			return null;
		} catch (SQLException e) {
			Capitol.LOGGER.error("Error while getting chunk from database.", e);
			throw new RuntimeException(e);
		}
	}
}
