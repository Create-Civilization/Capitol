package com.createcivilization.capitol.server.utils;

import com.createcivilization.capitol.common.assets.Team;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.List;

public class GsonUtil {

	public static final Type LIST_TEAMS_TYPE = new TypeToken<List<Team>>() {}.getType();

	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	public static String serialize(Team team) {
		return GSON.toJson(team);
	}
	public static String serializeList(List<Team> teams) {
		return GSON.toJson(teams);
	}
	public static Team deserializeTeam(String json) {
		return GSON.fromJson(json, Team.class);
	}
	public static List<Team> deserializeList(String json) {
		return GSON.fromJson(json, LIST_TEAMS_TYPE);
	}
}