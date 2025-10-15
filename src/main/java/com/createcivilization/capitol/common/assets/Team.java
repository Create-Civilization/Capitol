package com.createcivilization.capitol.common.assets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A representation of a team in capitol.
 * @param name The name of the team.
 * @param teamId The unique identifier of the team.
 * @param members A hashmap of every player in the team UUID being the player's UUID and the Integer acts like a
 *                reference to the role, each integer is the index of the role in the role hashmap.
 */
public record Team(
	String name,
	UUID teamId,
	HashMap<UUID, Integer> members
) {
	public Team(String name) {
		this(name, UUID.randomUUID(), new HashMap<>());
	}

	public Team(String name, UUID founder) {
		this(name, UUID.randomUUID(), new HashMap<>(Map.of(founder, 0)));
	}
}
