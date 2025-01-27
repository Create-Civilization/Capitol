package com.createcivilization.capitol.team;

import com.createcivilization.capitol.util.*;

import java.util.*;

public class War {

	private final Team
		declare,
		receive;

	public War(Team declare, Team receive) {
		this.declare = declare;
		this.receive = receive;
		LogToDiscord.postIfAllowed(this.declare, "War started! " + this);
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

	public List<UUID> getDeclaringTeamAndAlliesUUIDs() {
		List<UUID> UUIDs = new ArrayList<>();
		this.getDeclaringTeamAndAllies().forEach((team) -> UUIDs.addAll(team.getAllPlayers()));
		return UUIDs;
	}

	public List<UUID> getReceivingTeamAndAlliesUUIDs() {
		List<UUID> UUIDs = new ArrayList<>();
		this.getReceivingTeamAndAllies().forEach((team) -> UUIDs.addAll(team.getAllPlayers()));
		return UUIDs;
	}

	@Override
	public String toString() {
		return declare.getQuotedName() + " vs " + receive.getQuotedName();
	}
}