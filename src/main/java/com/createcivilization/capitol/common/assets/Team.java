package com.createcivilization.capitol.common.assets;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
