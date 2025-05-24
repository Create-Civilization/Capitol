package com.createcivilization.capitol.team;

import com.createcivilization.capitol.event.custom.WarEvent;
import com.createcivilization.capitol.util.*;

import com.mojang.datafixers.util.Pair;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class War {

	private final String
		declareId,
		receiveId;

	public final long timeOfCreation;

	public War(Team declare, Team receive, long time) {
		this(declare.getTeamId(), receive.getTeamId(), time);
	}

	public War(Team declare, Team receive) {
		this(declare, receive, System.currentTimeMillis() / 1000);
	}

	public War(String declareId, String receiveId, long time) {
		this.declareId = declareId;
		this.receiveId = receiveId;
		this.timeOfCreation = time;
		NeoForge.EVENT_BUS.post(new WarEvent.WarCreatedEvent(this));
		LogToDiscord.postIfAllowed(this.declareId, "War started! " + this);
	}

	public War(String declareId, String receiveId) {
		this(declareId, receiveId, System.currentTimeMillis() / 1000);
	}

	public Team getDeclaringTeam() {
		return TeamUtils.getTeam(declareId).getOrThrow();
	}

	public Team getReceivingTeam() {
		return TeamUtils.getTeam(receiveId).getOrThrow();
	}

	public List<Team> getDeclaringTeamAndAllies() {
		return TeamUtils.getTeamAndAllies(getDeclaringTeam());
	}

	public List<Team> getReceivingTeamAndAllies() {
		return TeamUtils.getTeamAndAllies(getReceivingTeam());
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
		return getDeclaringTeam().getQuotedName() + " vs " + getReceivingTeam().getQuotedName();
	}

	/**
	 * Returns a {@link Pair} of two lists containing the wars in which the given team is participating.
	 * <p>
	 * The first list contains the wars where the team is defending (i.e., the team or its allies are the targets of a declaration),
	 * and the second list contains the wars where the team is attacking (i.e., the team or its allies initiated the war).
	 * </p>
	 *
	 * @param team The team whose war participation is to be analyzed.
	 * @return A {@code Pair} of lists:
	 *         - {@code first}: List of wars where the team is a defender.
	 *         - {@code second}: List of wars where the team is an attacker.
	 */
	public static Pair<List<War>, List<War>> getParticipatingWars(Team team) {
		List<War> defending = new ArrayList<>();
		List<War> attacking = new ArrayList<>();
		String teamId = team.getTeamId();

		for (War loadedWar : TeamUtils.loadedWars) {
			if (loadedWar.getDeclaringTeamAndAllies().stream().map(Team::getTeamId).toList().contains(teamId)) attacking.add(loadedWar);
			else if (loadedWar.getReceivingTeamAndAllies().stream().map(Team::getTeamId).toList().contains(teamId)) defending.add(loadedWar);
		}

		return new Pair<>(defending, attacking);
	}

	public static List<War> getFlatParticipatingWars(Team team) {
		return flattenWarPair(War.getParticipatingWars(team));
	}

	private static List<War> flattenWarPair(Pair<List<War>, List<War>> participatingWarsPair) {
		List<War> participatingWars = new ArrayList<>(participatingWarsPair.getSecond());
		participatingWars.addAll(participatingWarsPair.getFirst());
		return participatingWars;
	}
}