package com.createcivilization.capitol.common.data;

import com.createcivilization.capitol.common.assets.Request;
import com.createcivilization.capitol.common.assets.Status;
import com.createcivilization.capitol.common.assets.Team;
import com.createcivilization.capitol.server.ServerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data manager for all relevant team data.
 */
public class TeamData {

	private static final List<Team> TEAMS = new ArrayList<>();

	/**
	 * Utilities that return a Status and perform checks and higher level actions.
	 */
	public static class SmartUtils {
		public static Status createTeam(Request request, String name) {
			// check if allowed to
			UUID origin = request.origin();
			if (origin == ServerConstants.SERVER_UUID)
				TEAMS.add(new Team(name));
			else if (!BaseUtils.playerHasTeam(origin)) {
				TEAMS.add(new Team(name, origin));
			} else
				return new Status(
					false,
					"Player already has a team.",
					null
				);

			return new Status(
				true,
				"Team successfully created.",
				null
			);
		}
	}

	/**
	 * Utilities that perform basic actions and checks.
	 */
	public static class BaseUtils {
		public static boolean playerHasTeam(UUID player) {
			return TEAMS.stream().anyMatch(team -> team.members().containsKey(player));
		}
	}

	// Temporary
	public static List<Team> getTeams() {
		return TEAMS;
	}
}
