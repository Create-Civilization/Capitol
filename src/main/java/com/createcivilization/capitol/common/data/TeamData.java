package com.createcivilization.capitol.common.data;

import com.createcivilization.capitol.common.classes.Request;
import com.createcivilization.capitol.common.classes.Status;
import com.createcivilization.capitol.common.classes.Team;
import com.createcivilization.capitol.server.ServerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamData {

	private static final List<Team> TEAMS = new ArrayList<>();

	public static Status createTeam(Request request, String name) {
		// check if allowed to
		UUID origin = request.origin();
		if (origin == ServerConstants.SERVER_UUID)
			TEAMS.add(new Team(name));
		else if (!playerHasTeam(origin)) {
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

	public static boolean playerHasTeam(UUID player) {
		return TEAMS.stream().anyMatch(team -> team.members().containsKey(player));
	}

	//Temporary
	public static List<Team> getTeams() {
		return TEAMS;
	}
}
