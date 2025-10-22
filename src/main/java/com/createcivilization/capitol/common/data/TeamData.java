package com.createcivilization.capitol.common.data;

import com.createcivilization.capitol.common.assets.Request;
import com.createcivilization.capitol.common.assets.Role;
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

	public static final File DATA_FILE = new File(ServerConstants.CAPITOL_FOLDER, "team_data.json");
	private static final List<Team> TEAMS = new ArrayList<>();

	/**
	 * Utilities that return a Status and perform checks and higher level actions.
	 */
	public static class SmartUtils {
		public static Status createTeam(Request request, String name) {
			UUID origin = request.origin();
			Team newTeam;
			if (origin.equals(ServerConstants.SERVER_UUID))
				// Is server
				newTeam = new Team(name);
			else if (!BaseUtils.playerHasTeam(origin)) {
				// Is player
				newTeam = new Team(name, origin);
			} else
				return new Status(
					false,
					"Player already has a team.",
					null
				);

			TEAMS.add(newTeam);
			ClaimData.BaseUtils.startTeam(newTeam.teamId());

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
	 * Utilities that perform basic actions and checks without returning a Status.
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

		public static Role getPlayerRole(UUID player) {
			Team playerTeam = getPlayerTeam(player);
			return playerTeam.roles().get(playerTeam.members().get(player));
		}

		public static boolean hasPermission(UUID player, String permission) {
			return getPlayerRole(player).permissionMap().get(permission);
		}

		// Team Methods
		public static void removeTeam(UUID teamId) {
			TEAMS.removeIf(team -> team.teamId().equals(teamId));
		}

		public static void setTeams(List<Team> teams) {
			TEAMS.clear();
			TEAMS.addAll(teams);
		}

		public static Team getTeam(UUID teamId) {
			return TEAMS.stream().filter(team -> team.teamId().equals(teamId)).toList().getFirst();
		}
	}

	/**
	 * Utilities that manage teams data wise
	 */
	public static class DataUtils {
		public static void loadData() throws IOException {
			if (!ServerConstants.CAPITOL_FOLDER.exists() || !DATA_FILE.exists()) return;
			FileReader fileReader = new FileReader(DATA_FILE);
			StringBuilder string = new StringBuilder();

			int charInt;

			while ((charInt = fileReader.read()) != -1) {
				string.append((char) charInt);
			}

			BaseUtils.setTeams(GsonUtil.deserializeTeamList(string.toString()));
		}

		public static void saveData() throws IOException {
			if (!ServerConstants.CAPITOL_FOLDER.exists()) if (!ServerConstants.CAPITOL_FOLDER.mkdir()) throw new IOException("Capitol Data folder could not be made.");
			if (!DATA_FILE.exists()) {
				if (!DATA_FILE.createNewFile()) throw new IOException("Team Data file could not be made.");
				else {
					DATA_FILE.setWritable(true);
					DATA_FILE.setReadable(true);
				}
			}

			FileWriter fileWriter = new FileWriter(DATA_FILE);
			fileWriter.write(GsonUtil.serializeList(TEAMS));
			fileWriter.close();
		}
	}

	// Temporary
	public static List<Team> getTeams() {
		return TEAMS;
	}
}
