package com.createcivilization.capitol.common.data;

import com.createcivilization.capitol.common.assets.Request;
import com.createcivilization.capitol.common.assets.Role;
import com.createcivilization.capitol.common.assets.Status;
import com.createcivilization.capitol.common.assets.Team;
import com.createcivilization.capitol.server.ServerConstants;

import javax.annotation.Nullable;
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
				"Team" + name + " successfully created.",
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

		public static Role getPlayerRole(UUID player) {
			Team playerTeam = getPlayerTeam(player);
			return playerTeam.roles().get(playerTeam.members().get(player));
		}

		public static boolean isOwner(UUID player) {
			// Filters to see if player has the role pointer of 0, which has to be the owner role as it cannot be mutated.
			return TEAMS.stream().anyMatch(team -> team.members().entrySet().stream().anyMatch(entry -> entry.getKey() == player && entry.getValue() == 0));
		}

		public static void removeTeam(UUID teamId) {
			TEAMS.removeIf(team -> team.teamId().equals(teamId));
		}
	}

	// Temporary
	public static List<Team> getTeams() {
		return TEAMS;
	}
}
