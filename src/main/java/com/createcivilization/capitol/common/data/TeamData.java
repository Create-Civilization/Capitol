package com.createcivilization.capitol.common.data;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.assets.Request;
import com.createcivilization.capitol.common.assets.Status;
import com.createcivilization.capitol.common.assets.Team;
import com.createcivilization.capitol.server.ServerConstants;
import com.createcivilization.capitol.server.utils.GsonUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data manager for all relevant team data.
 */
public class TeamData {

	public static final File CAPITOL_FOLDER = new File(System.getProperty("user.dir"), "capitol_data");
	public static final File TEAM_DATA_FILE = new File(CAPITOL_FOLDER, "team_data.json");
	private static final List<Team> TEAMS = new ArrayList<>();

	/**
	 * Utilities that return a Status and perform checks and higher level actions.
	 */
	public static class SmartUtils {
		public static Status createTeam(Request request, String name) {
			UUID origin = request.origin();
			if (origin.equals(ServerConstants.SERVER_UUID))
				// Is server
				TEAMS.add(new Team(name));
			else if (!BaseUtils.playerHasTeam(origin)) {
				// Is player
				TEAMS.add(new Team(name, origin));
			} else
				return new Status(
					false,
					"Player already has a team.",
					null
				);

			return new Status(
				true,
				"Team " + name + " successfully created.",
				null
			);
		}

		public static Status disbandTeam(Request request, @Nullable UUID teamId) {
			UUID origin = request.origin();
			if (origin.equals(ServerConstants.SERVER_UUID))
				BaseUtils.removeTeam(teamId);
			else if (BaseUtils.isOwner(origin))
				BaseUtils.removeTeam(BaseUtils.getPlayerTeam(origin).teamId());
			else
				return new Status(
					false,
					"You must be the owner of the team to disband it!",
					null
				);

			return new Status(
				true,
				"Team successfully disbanded.",
				null
			);
		}
	}

	/**
	 * Utilities that perform basic actions and checks.
	 */
	public static class BaseUtils {

		// Player Methods
		public static boolean playerHasTeam(UUID player) {
			return TEAMS.stream().anyMatch(team -> team.members().containsKey(player));
		}

		public static Team getPlayerTeam(UUID player) {
			return TEAMS.stream().filter(team -> team.members().containsKey(player)).toList().getFirst();
		}

		public static boolean isOwner(UUID player) {
			// Filters to see if player has the role pointer of 0, which has to be the owner role as it cannot be mutated.
			return TEAMS.stream().anyMatch(team -> team.members().entrySet().stream().anyMatch(entry -> entry.getKey() == player && entry.getValue() == 0));
		}

		public static void removeTeam(UUID teamId) {
			TEAMS.removeIf(team -> team.teamId().equals(teamId));
		}
	}

	/**
	 * Utilities that manage teams data wise
	 */
	public static class DataUtils {
		public static void loadData() throws IOException {
			Capitol.LOGGER.info("Loading capitol data..");
			FileReader fileReader = new FileReader(TEAM_DATA_FILE);
			StringBuilder string = new StringBuilder();

			int charInt;

			while ((charInt = fileReader.read()) != -1) {
				string.append((char) charInt);
			}

			TeamData.setTeams(GsonUtil.deserializeList(string.toString()));

			Capitol.LOGGER.info("Capitol data loaded successfully.");
		}

		public static void saveData() throws IOException {
			Capitol.LOGGER.info("Saving capitol data..");
			if (!CAPITOL_FOLDER.exists()) if (!CAPITOL_FOLDER.mkdir()) throw new IOException("Capitol Data folder could not be made.");
			if (!TEAM_DATA_FILE.exists()) {
				if (!TEAM_DATA_FILE.createNewFile()) throw new IOException("Team Data file could not be made.");
				else {
					TEAM_DATA_FILE.setWritable(true);
					TEAM_DATA_FILE.setReadable(true);
				}
			}

			FileWriter fileWriter = new FileWriter(TEAM_DATA_FILE);
			fileWriter.write(GsonUtil.serializeList(TEAMS));
			fileWriter.close();
			Capitol.LOGGER.info("Capitol data saved successfully.");
		}
	}

	// Temporary
	public static List<Team> getTeams() {
		return TEAMS;
	}

	public static void setTeams(List<Team> teams) {
		TEAMS.clear();
		TEAMS.addAll(teams);
	}
}
