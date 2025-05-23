package com.createcivilization.capitol.team;

import com.createcivilization.capitol.event.custom.WarEvent;
import com.createcivilization.capitol.util.*;

import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class War {

	private final Team
		declare,
		receive;

	public final long timeOfCreation;

	public War(Team declare, Team receive, long time) {
		this.declare = declare;
		this.receive = receive;
		this.timeOfCreation = time;
		NeoForge.EVENT_BUS.post(new WarEvent.WarCreatedEvent(this));
		LogToDiscord.postIfAllowed(this.declare, "War started! " + this);
	}

	public War(Team declare, Team receive) {
		this(declare, receive, System.currentTimeMillis() / 1000);
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