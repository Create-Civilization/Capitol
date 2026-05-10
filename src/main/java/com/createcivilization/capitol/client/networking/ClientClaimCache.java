package com.createcivilization.capitol.client.networking;

import com.createcivilization.capitol.common.data.Team;
import net.minecraft.world.level.ChunkPos;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientClaimCache {

	public static final Map<ChunkPos, Team> claims = new ConcurrentHashMap<>();

	public static Map<ChunkPos, Team> get(){
		return claims;
	}

	public static void addClaim(ChunkPos cords, Team team) {
		claims.put(cords, team);
	}

	public static void removeClaim(ChunkPos cords) {
		claims.remove(cords);
	}

	public static Team getClaim(ChunkPos cords) {
		return claims.get(cords);
	}

	public static boolean hasClaim(ChunkPos cords) {
		return claims.containsKey(cords);
	}

	public static void clearClaims() {
		claims.clear();
	}

}
