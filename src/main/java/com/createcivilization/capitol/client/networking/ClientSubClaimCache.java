package com.createcivilization.capitol.client.networking;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientSubClaimCache {

	// keyed by subclaim UUID
	public static final Map<UUID, SubClaimRenderData> subclaims = new ConcurrentHashMap<>();

	public static void add(UUID id, SubClaimRenderData data) {
		subclaims.put(id, data);
	}

	public static void remove(UUID id) {
		subclaims.remove(id);
	}

	public static void clear() {
		subclaims.clear();
	}
}