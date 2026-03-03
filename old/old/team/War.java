package com.createcivilization.capitol.old.old.team;

import com.createcivilization.capitol.old.old.event.custom.WarEvent;

import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.old.util.team.LogToDiscord;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import com.mojang.datafixers.util.Pair;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

public class War {

	private final String
		declareId,
		receiveId;

	public final long timeOfCreation;

	public War(OldTeam declare, OldTeam receive, long time) {
		this(declare.getTeamId(), receive.getTeamId(), time);
	}

	public War(OldTeam declare, OldTeam receive) {
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

	public OldTeam getDeclaringTeam() {
		return TeamUtils.getTeam(declareId).getOrThrow();
	}

	public OldTeam getReceivingTeam() {
		return TeamUtils.getTeam(receiveId).getOrThrow();
	}

	public String getDeclaringTeamId() {
		return declareId;
	}

	public String getReceivingTeamId() {
		return receiveId;
	}

	public long getTimeOfCreation() {
		return timeOfCreation;
	}

	public List<OldTeam> getDeclaringTeamAndAllies() {
		return TeamUtils.getTeamAndAllies(getDeclaringTeam());
	}

	public List<OldTeam> getReceivingTeamAndAllies() {
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
	 * Returns a {@link Pair} of two lists containing the wars in which the given oldTeam is participating.
	 * <p>
	 * The first list contains the wars where the oldTeam is defending (i.e., the oldTeam or its allies are the targets of a declaration),
	 * and the second list contains the wars where the oldTeam is attacking (i.e., the oldTeam or its allies initiated the war).
	 * </p>
	 *
	 * @param oldTeam The oldTeam whose war participation is to be analyzed.
	 * @return A {@code Pair} of lists:
	 *         - {@code first}: List of wars where the oldTeam is a defender.
	 *         - {@code second}: List of wars where the oldTeam is an attacker.
	 */
	public static Pair<List<War>, List<War>> getParticipatingWars(OldTeam oldTeam) {
		List<War> defending = new ArrayList<>();
		List<War> attacking = new ArrayList<>();
		String teamId = oldTeam.getTeamId();

		for (War loadedWar : DataManager.WarData.loadedWars) {
			if (loadedWar.getDeclaringTeamAndAllies().stream().map(OldTeam::getTeamId).toList().contains(teamId)) attacking.add(loadedWar);
			else if (loadedWar.getReceivingTeamAndAllies().stream().map(OldTeam::getTeamId).toList().contains(teamId)) defending.add(loadedWar);
		}

		return new Pair<>(defending, attacking);
	}

	public static List<War> getFlatParticipatingWars(OldTeam oldTeam) {
		return flattenWarPair(War.getParticipatingWars(oldTeam));
	}

	private static List<War> flattenWarPair(Pair<List<War>, List<War>> participatingWarsPair) {
		List<War> participatingWars = new ArrayList<>(participatingWarsPair.getSecond());
		participatingWars.addAll(participatingWarsPair.getFirst());
		return participatingWars;
	}
}