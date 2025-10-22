package com.createcivilization.capitol.server.utils;

import com.createcivilization.capitol.common.assets.Team;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GsonUtil {

	public static final Type LIST_TEAMS_TYPE = new TypeToken<List<Team>>() {}.getType();
	public static final Type LIST_CHUNKS_TYPE = new TypeToken<List<ChunkPos>>() {}.getType();

	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	public static String serializeList(List<Team> teams) {
		return GSON.toJson(teams);
	}
	public static List<Team> deserializeTeamList(String json) {
		return GSON.fromJson(json, LIST_TEAMS_TYPE);
	}

	public static Map<UUID, Map<ResourceLocation, List<ChunkPos>>> deserializeChunkMap(String json) {
		return GSON.fromJson(json, LIST_CHUNKS_TYPE);
	}

	public static String serializeChunkMap(Map<UUID, Map<ResourceLocation, List<ChunkPos>>> chunks) {
		return GSON.toJson(chunks);
	}
}