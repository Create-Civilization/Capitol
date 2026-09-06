package com.createcivilization.capitol.common.managers;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.compat.sable.SableCompat;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
					"created_at LONG NOT NULL," +
					"capitol_x INTEGER," +
					"capitol_y INTEGER," +
					"capitol_z INTEGER," +
					"capitol_dimension TEXT)"
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
					"capitol_block_id INTEGER," +  // the capitol block this chunk falls under, if any
					"PRIMARY KEY (dimension, chunk_x, chunk_z)," +
					"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
			);

			stmt.execute(
				"CREATE TABLE IF NOT EXISTS capitol_blocks (" +
					"id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"team_id TEXT NOT NULL," +
					"dimension TEXT NOT NULL," +
					"x INTEGER NOT NULL," +
					"y INTEGER NOT NULL," +
					"z INTEGER NOT NULL," +
					"is_capital INTEGER NOT NULL DEFAULT 0," +
					"tier TEXT," +  // VILLAGE/TOWN/CITY; null for the Capital
					"name TEXT NOT NULL UNIQUE," +  // globally unique, set when the block is named
					"mayor_uuid TEXT," +  // who runs this block, defaults to the team leader
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

	private static void runMigrations(int current) throws SQLException {
		if (current < 1) {
			// Databases from before capitol position tracking lack these columns.
			// Fresh databases get them from createTables() instead, so only migrate
			// when an old teams table actually exists.
			if (tableExists("teams") && !columnExists("teams", "capitol_x")) {
				try (Statement stmt = connection.createStatement()) {
					stmt.execute("ALTER TABLE teams ADD COLUMN capitol_x INTEGER");
					stmt.execute("ALTER TABLE teams ADD COLUMN capitol_y INTEGER");
					stmt.execute("ALTER TABLE teams ADD COLUMN capitol_z INTEGER");
					stmt.execute("ALTER TABLE teams ADD COLUMN capitol_dimension TEXT");
				}
			}
			setSchemaVersion(1);
		}
		if (current < 2) {
			try (Statement stmt = connection.createStatement()) {
				stmt.execute(
					"CREATE TABLE IF NOT EXISTS capitol_blocks (" +
						"id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"team_id TEXT NOT NULL," +
						"dimension TEXT NOT NULL," +
						"x INTEGER NOT NULL," +
						"y INTEGER NOT NULL," +
						"z INTEGER NOT NULL," +
						"is_capital INTEGER NOT NULL DEFAULT 0," +
						"FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE)"
				);
				// Any capitol block that predates this migration becomes the team's Capital.
				if (tableExists("teams") && columnExists("teams", "capitol_x")) {
					stmt.execute(
						"INSERT INTO capitol_blocks (team_id, dimension, x, y, z, is_capital) " +
							"SELECT id, capitol_dimension, capitol_x, capitol_y, capitol_z, 1 FROM teams " +
							"WHERE capitol_x IS NOT NULL AND capitol_dimension IS NOT NULL"
					);
				}
			}
			setSchemaVersion(2);
		}
		if (current < 3) {
			// capitol blocks get a settlement tier; the Capital doesn't have one
			if (tableExists("capitol_blocks") && !columnExists("capitol_blocks", "tier")) {
				try (Statement stmt = connection.createStatement()) {
					stmt.execute("ALTER TABLE capitol_blocks ADD COLUMN tier TEXT");
					// old extra blocks start as Villages
					stmt.execute("UPDATE capitol_blocks SET tier = 'VILLAGE' WHERE is_capital = 0 AND tier IS NULL");
				}
			}
			// chunks now remember which capitol block they belong to
			if (tableExists("chunks") && !columnExists("chunks", "capitol_block_id")) {
				try (Statement stmt = connection.createStatement()) {
					stmt.execute("ALTER TABLE chunks ADD COLUMN capitol_block_id INTEGER");
				}
				backfillCapitolBlockChunks();
			}
			setSchemaVersion(3);
		}
		if (current < 4) {
			// blocks get a globally-unique name (set at placement) + a mayor
			if (tableExists("capitol_blocks") && !columnExists("capitol_blocks", "name")) {
				try (Statement stmt = connection.createStatement()) {
					stmt.execute("ALTER TABLE capitol_blocks ADD COLUMN name TEXT");
					stmt.execute("ALTER TABLE capitol_blocks ADD COLUMN mayor_uuid TEXT");
					// old blocks get a placeholder name so the unique constraint has something to hold
					stmt.execute("UPDATE capitol_blocks SET name = CASE WHEN is_capital = 1 THEN 'Capital ' || id ELSE 'Village ' || id END WHERE name IS NULL");
					// sqlite can't add UNIQUE to an existing column, so use a unique index instead
					stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_capitol_blocks_name ON capitol_blocks(name)");
				}
			}
			setSchemaVersion(4);
		}
	}

	// old dbs don't have chunk attribution, so we work it out from how the
	// chunks were claimed back then (7x7 around each block on placement)
	private static void backfillCapitolBlockChunks() throws SQLException {
		if (!tableExists("capitol_blocks") || !tableExists("chunks")) return;
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(
				 "SELECT id, team_id, dimension, x, z FROM capitol_blocks ORDER BY id")) {
			List<CapitolBlockRow> blocks = new ArrayList<>();
			while (rs.next()) {
				// block pos -> chunk pos of the chunk it sits in
				blocks.add(new CapitolBlockRow(
					rs.getLong("id"),
					rs.getString("team_id"),
					rs.getString("dimension"),
					rs.getInt("x") >> 4,
					rs.getInt("z") >> 4
				));
			}
			for (CapitolBlockRow block : blocks) {
				// chunks were claimed in a 7x7 (radius 3) around the block when placed
				try (PreparedStatement update = connection.prepareStatement(
					"UPDATE chunks SET capitol_block_id = ? " +
						"WHERE dimension = ? AND chunk_x = ? AND chunk_z = ? AND team_id = ? " +
						"AND capitol_block_id IS NULL")) {
					for (int dx = -3; dx <= 3; dx++) {
						for (int dz = -3; dz <= 3; dz++) {
							update.setLong(1, block.id);
							update.setString(2, block.dimension);
							update.setInt(3, block.chunkX + dx);
							update.setInt(4, block.chunkZ + dz);
							update.setString(5, block.teamId);
							update.executeUpdate();
						}
					}
				}
			}
		}
	}

	private record CapitolBlockRow(long id, String teamId, String dimension, int chunkX, int chunkZ) {}


	private static boolean tableExists(String table) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement(
			"SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?")) {
			ps.setString(1, table);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next();
			}
		}
	}

	private static boolean columnExists(String table, String column) throws SQLException {
		try (PreparedStatement ps = connection.prepareStatement("PRAGMA table_info(" + table + ")")) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					if (column.equals(rs.getString("name"))) return true;
				}
			}
		}
		return false;
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