package com.createcivilization.capitol.common.managers;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.compat.sable.SableCompat;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;

import java.nio.file.Path;
import java.sql.*;

public class DatabaseManager {

	public static CapitolDatabase database = new CapitolDatabase();


	private static Connection connection;

	public static void init(Path saveFolder){
		Capitol.LOGGER.info("Attempting to initialize database");
		try{
			Class.forName("org.sqlite.JDBC");
			Path databasePath = saveFolder.resolve("capitol.db");
			String url = "jdbc:sqlite:" + databasePath.toAbsolutePath();

			connection = DriverManager.getConnection(url);

			int version = getSchemaVersion();
			runMigrations(version);

			try (Statement stmt = connection.createStatement()) {
				stmt.execute("PRAGMA journal_mode=WAL;");
				stmt.execute("PRAGMA synchronous=NORMAL;");
				stmt.execute("PRAGMA foreign_keys=ON;");
			}

			createTables();
			Capitol.LOGGER.info("Capitol Database initialized");

			Capitol.LOGGER.info("Warming Cache");
			database.warmCache();

		} catch (ClassNotFoundException e) {
			Capitol.LOGGER.error("No SQLite driver found.", e);
		} catch (SQLException e) {
			Capitol.LOGGER.error("Failed to initialize capitol database", e);
			// don't leave a broken connection open
			try {
				if (connection != null) connection.close();
			} catch (SQLException ignored) {}
		}

	}

	private static void createTables() throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute(
				"CREATE TABLE IF NOT EXISTS teams (" +
					"id TEXT PRIMARY KEY NOT NULL," +
					"name TEXT NOT NULL UNIQUE," +
					"tag TEXT NOT NULL," +
					"current_claims INT NOT NULL, " +
					"max_claims INT," +
					"color INT NOT NULL," +
					"description TEXT," +
					"team_permissions INT NOT NULL," +
					"created_at LONG NOT NULL)"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS team_roles (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"team_id TEXT NOT NULL," +
					"name TEXT NOT NULL," +
					"permissions INTEGER NOT NULL DEFAULT 0," +
					"UNIQUE (team_id, name)," +
					"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS team_members (" +
					"team_id TEXT NOT NULL," +
					"player_uuid TEXT NOT NULL," +
					"role_id INTEGER NOT NULL," +
					"PRIMARY KEY (team_id, player_uuid)," +
					"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE," +
					"FOREIGN KEY (role_id) REFERENCES team_roles (id) ON DELETE RESTRICT)"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS chunks (" +
					"dimension TEXT NOT NULL," +  // Should be like "minecraft:overwold" ect
					"chunk_x INTEGER NOT NULL," +
					"chunk_z INTEGER NOT NULL," +
					"team_id TEXT NOT NULL," +
					"force_loaded BOOLEAN NOT NULL," +
					"PRIMARY KEY (dimension, chunk_x, chunk_z)," +
					"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
			);


			stmt.execute(
				"CREATE TABLE IF NOT EXISTS player_permissions (" +
					"team_id TEXT NOT NULL," +
					"player_uuid TEXT NOT NULL," +
					"permissions INTEGER NOT NULL DEFAULT 0," +
					"PRIMARY KEY (team_id, player_uuid)," +
					"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS sub_claims (" +
					"id TEXT PRIMARY KEY NOT NULL," +
					"team_id TEXT NOT NULL," +
					"name TEXT NOT NULL," +
					"dimension TEXT NOT NULL," +
					"min_x INTEGER NOT NULL," +
					"min_y INTEGER NOT NULL," +
					"min_z INTEGER NOT NULL," +
					"max_x INTEGER NOT NULL," +
					"max_y INTEGER NOT NULL," +
					"max_z INTEGER NOT NULL," +
					"owner_uuid TEXT NOT NULL," +
					"permissions INTEGER NOT NULL DEFAULT 0," +
					"protections INTEGER NOT NULL DEFAULT 0," +
					"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
			);

			if(SableCompat.LOADED){
				stmt.execute(
					"CREATE TABLE IF NOT EXISTS sub_levels (" +
						"id TEXT NOT NULL," +
						"team_id TEXT NOT NULL," +
						"PRIMARY KEY (id)," +
						"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
				);
			} else {
				stmt.execute(
					"DROP TABLE IF EXISTS sub_levels"
				);
			}

		}
	}

	//Will be used for DB migrations ect
	private static void runMigrations(int current) throws SQLException {
		if (current < 1) {
			try (Statement stmt = connection.createStatement()) {
				stmt.execute(
					"CREATE TABLE IF NOT EXISTS sub_claims (" +
						"id TEXT PRIMARY KEY NOT NULL," +
						"team_id TEXT NOT NULL," +
						"name TEXT NOT NULL," +
						"dimension TEXT NOT NULL," +
						"min_x INTEGER NOT NULL," +
						"min_y INTEGER NOT NULL," +
						"min_z INTEGER NOT NULL," +
						"max_x INTEGER NOT NULL," +
						"max_y INTEGER NOT NULL," +
						"max_z INTEGER NOT NULL," +
						"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
				);
			}
			setSchemaVersion(1);
		}
		if (current < 2) {
			try (Statement stmt = connection.createStatement()) {
				// old saves can be at v1 but never got the table
				stmt.execute(
					"CREATE TABLE IF NOT EXISTS sub_claims (" +
						"id TEXT PRIMARY KEY NOT NULL," +
						"team_id TEXT NOT NULL," +
						"name TEXT NOT NULL," +
						"dimension TEXT NOT NULL," +
						"min_x INTEGER NOT NULL," +
						"min_y INTEGER NOT NULL," +
						"min_z INTEGER NOT NULL," +
						"max_x INTEGER NOT NULL," +
						"max_y INTEGER NOT NULL," +
						"max_z INTEGER NOT NULL," +
						"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
				);
				// old sub-claims get an empty owner and default bitfields
				stmt.execute("ALTER TABLE sub_claims ADD COLUMN owner_uuid TEXT NOT NULL DEFAULT ''");
				stmt.execute("ALTER TABLE sub_claims ADD COLUMN permissions INTEGER NOT NULL DEFAULT 0");
			}
			setSchemaVersion(2);
		}
		if (current < 3) {
			try (Statement stmt = connection.createStatement()) {
				// same guard, in case the table is missing at v2
				stmt.execute(
					"CREATE TABLE IF NOT EXISTS sub_claims (" +
						"id TEXT PRIMARY KEY NOT NULL," +
						"team_id TEXT NOT NULL," +
						"name TEXT NOT NULL," +
						"dimension TEXT NOT NULL," +
						"min_x INTEGER NOT NULL," +
						"min_y INTEGER NOT NULL," +
						"min_z INTEGER NOT NULL," +
						"max_x INTEGER NOT NULL," +
						"max_y INTEGER NOT NULL," +
						"max_z INTEGER NOT NULL," +
						"owner_uuid TEXT NOT NULL," +
						"permissions INTEGER NOT NULL DEFAULT 0," +
						"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
				);
				stmt.execute("ALTER TABLE sub_claims ADD COLUMN protections INTEGER NOT NULL DEFAULT 0");
			}
			setSchemaVersion(3);
		}
	}

	private static int getSchemaVersion() throws SQLException {
		try (ResultSet rs = connection.createStatement().executeQuery("PRAGMA user_version")) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}

	private static void setSchemaVersion(int version) throws SQLException {
		connection.createStatement().execute("PRAGMA user_version = " + version);
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