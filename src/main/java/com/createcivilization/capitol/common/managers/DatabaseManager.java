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

			try (Statement stmt = connection.createStatement()) {
				stmt.execute("PRAGMA journal_mode=WAL;");
				stmt.execute("PRAGMA synchronous=NORMAL;");
				stmt.execute("PRAGMA foreign_keys=ON;");
			}

			createTables();
			Capitol.LOGGER.info("Capitol Database tables initialized");

			int version = getSchemaVersion();
			runMigrations(version);

			Capitol.LOGGER.info("Capitol Database initialized");

			Capitol.LOGGER.info("Warming Cache");
			database.warmCache();

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
					"color INTEGER NOT NULL DEFAULT -1,"+
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

	public static int SCHEMA_VERSION = 2;
	//Will be used for DB migrations ect
	private static void runMigrations(int current) throws SQLException {
		if (SCHEMA_VERSION == current){
			return;
		}
		if (current == 0) { // a schema of 0 means that the database is brand new, thus no migrations are needed
			setSchemaVersion(SCHEMA_VERSION);
		}
		Capitol.LOGGER.info("Schema out of date, updating to version " + SCHEMA_VERSION + " from version " + current);
		int updates = SCHEMA_VERSION - current;
		for (int version = current; version <= updates; ++version) {
			// Runs for every update that has occured between now and the previous db version.
			// E.G, current version 2 -> desired version 4 will result in versions
			// 3 & 4 being run
			switch (version) {
				case 2:
					try (Statement stmt = connection.createStatement()) {
						stmt.execute("ALTER TABLE team_roles ADD COLUMN color INTEGER NOT NULL DEFAULT -1;");
					}
			}
		}
		setSchemaVersion(SCHEMA_VERSION);
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