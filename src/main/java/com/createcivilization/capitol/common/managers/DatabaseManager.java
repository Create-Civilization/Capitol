package com.createcivilization.capitol.common.managers;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

	public static CapitolDatabase database = new CapitolDatabase();


	private static Connection connection;

	public static void init(Path saveFolder){
		try{
			Class.forName("org.sqlite.JDBC");
			Path databasePath = saveFolder.resolve("capitol.db");
			String url = "jdbc:sqlite:" + databasePath.toAbsolutePath();

			connection = DriverManager.getConnection(url);

			try (Statement stmt = connection.createStatement()) {
				stmt.execute("PRAGMA journal_mode=WAL;");
				stmt.execute("PRAGMA synchronous=NORMAL;");
				stmt.execute("PRAGMA foreign_keys=ON;");
			}

			createTables();
			Capitol.LOGGER.info("Capitol Database initialized");

		} catch (ClassNotFoundException e) {
			Capitol.LOGGER.error("No SQLite driver found.", e);
		} catch (SQLException e) {
			Capitol.LOGGER.error("Failed to initialize capitol database", e);
		}

	}

	private static void createTables() throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(
				"CREATE TABLE IF NOT EXISTS teams (" +
					"id TEXT PRIMARY KEY NOT NULL," +
					"name TEXT NOT NULL," +
					"color INT NOT NULL," +
					"created_at LONG NOT NULL)"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS team_members (" +
					"team_id TEXT NOT NULL," +
					"player_uuid TEXT NOT NULL," +
					"role TEXT NOT NULL DEFAULT 'member'," + // 'member', 'officer', 'owner' (Change this if we want)
					"permissions INTEGER NOT NULL DEFAULT 0," +
					"PRIMARY KEY (team_id, player_uuid)," +
					"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS chunks (" +
					"dimension TEXT NOT NULL," +  // Should be like "minecraft:overwold" ect
					"chunk_x INTEGER NOT NULL," +
					"chunk_z INTEGER NOT NULL," +
					"team_id TEXT NOT NULL," +
					"PRIMARY KEY (dimension, chunk_x, chunk_z)," +
					"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
			);
		}
	}

	public static Connection getConnection() {
		return connection;
	}

	public static void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				Capitol.LOGGER.info("Capitol Database closed");
			}
		} catch (SQLException e) {
			Capitol.LOGGER.error("Failed to close Capitol database", e);
		}
	}

}