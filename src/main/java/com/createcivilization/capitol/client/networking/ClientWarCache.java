package com.createcivilization.capitol.client.networking;

import com.createcivilization.capitol.common.data.War;

import java.util.ArrayList;
import java.util.List;

// the client's copy of the server's war list, kept in sync via S2CSyncWars
public class ClientWarCache {

	private static List<War> wars = new ArrayList<>();

	public static void setWars(List<War> newWars) {
		wars = new ArrayList<>(newWars);
	}

	public static List<War> getWars() {
		return wars;
	}

	// all wars the given team is a direct participant in
	public static List<War> getWarsForTeam(java.util.UUID teamId) {
		return wars.stream().filter(war -> war.isParticipant(teamId)).toList();
	}

	public static void clear() {
		wars = new ArrayList<>();
	}
}