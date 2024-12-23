package com.createcivilization.capitol.team;

import com.createcivilization.capitol.util.TeamUtils;

import java.util.List;

public class War {

	private final Team
		declare,
		receive;

	public War(Team declare, Team receive) {
		this.declare = declare;
		this.receive = receive;
	}

	public Team getDeclaringTeam() {
		return this.declare;
	}

	public Team getReceivingTeam() {
		return this.receive;
	}

	public List<Team> getDeclaringTeamAndAllies() {
		return TeamUtils.getTeamAndAllies(this.declare);
	}

	public List<Team> getReceivingTeamAndAllies() {
		return TeamUtils.getTeamAndAllies(this.receive);
	}
}