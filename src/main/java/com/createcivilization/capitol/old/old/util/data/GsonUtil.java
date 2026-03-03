package com.createcivilization.capitol.old.old.util.data;

import com.createcivilization.capitol.old.old.team.Team;

import com.createcivilization.capitol.old.old.team.War;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import net.minecraft.resources.ResourceLocation;

import java.awt.Color;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class GsonUtil {

	public static final Type LIST_TEAMS_TYPE = new TypeToken<List<Team>>() {}.getType();
	public static final Type LIST_WARS_TYPE = new TypeToken<List<War>>() {}.getType();

	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.disableHtmlEscaping()
		.registerTypeAdapter(Color.class, new ColorAdapter())
		.registerTypeAdapter(UUID.class, new UUIDAdapter())
		.registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter())
		.registerTypeAdapter(Team.TeamDimensionData.class, new TeamDimensionDataAdapter())
		.registerTypeAdapter(War.class, new WarAdapter())
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

	public static Team.CapitolData deserializeCapitol(String json) {
		return GSON.fromJson(json, Team.CapitolData.class);
	}

	public static String serializeCapitol(Team.CapitolData capitolData) {
		return GSON.toJson(capitolData);
	}

	public static War deserializeWar(String json) {
		return GSON.fromJson(json, War.class);
	}

	public static String serializeWar(War capitolData) {
		return GSON.toJson(capitolData);
	}

	public static List<Team> deserializeList(String json) {
		return GSON.fromJson(json, LIST_TEAMS_TYPE);
	}

	public static void saveTeamToFile(List<Team> teams, String filePath) throws IOException {
		try (FileWriter writer = new FileWriter(filePath)) {
			GSON.toJson(teams, writer);
		}
	}

	public static void saveWarToFile(List<War> wars, String filePath) throws IOException {
		try (FileWriter writer = new FileWriter(filePath)) {
			GSON.toJson(wars, writer);
		}
	}

	public static List<Team> loadFromFile(String filePath) throws IOException {
		try (FileReader reader = new FileReader(filePath)) {
			return GSON.fromJson(reader, LIST_TEAMS_TYPE);
		}
	}

	public static List<Team> loadTeamsFromString(String json) {
		return GSON.fromJson(json, LIST_TEAMS_TYPE);
	}

	public static List<War> loadWarsFromString(String json) {
		return GSON.fromJson(json, LIST_WARS_TYPE);
	}

	static class ColorAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
		@Override
		public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.getRGB());
		}

		@Override
		public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return new Color(json.getAsInt(), true);
		}
	}

	static class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
		@Override
		public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}

		@Override
		public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return UUID.fromString(json.getAsString());
		}
	}

	static class ResourceLocationAdapter implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
		@Override
		public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.toString());
		}

		@Override
		public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return ResourceLocation.parse(json.getAsString());
		}
	}

	static class TeamDimensionDataAdapter implements JsonSerializer<Team.TeamDimensionData>, JsonDeserializer<Team.TeamDimensionData> {
		@Override
		public JsonElement serialize(Team.TeamDimensionData src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("capitolDataList", context.serialize(src.getCapitolDataList()));
			return jsonObject;
		}

		@Override
		public Team.TeamDimensionData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			Team.TeamDimensionData dimensionData = new Team.TeamDimensionData();
			JsonObject jsonObject = json.getAsJsonObject();
			JsonArray capitolDataList = jsonObject.getAsJsonArray("capitolDataList");

			for (JsonElement element : capitolDataList) {
				dimensionData.addCapitolData(context.deserialize(element, Team.CapitolData.class));
			}
			return dimensionData;
		}
	}

	static class WarAdapter implements JsonSerializer<War>, JsonDeserializer<War> {
		@Override
		public War deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObject = json.getAsJsonObject();

			String declareId = jsonObject.get("declareId").getAsString();
			String receiveId = jsonObject.get("receiveId").getAsString();
			long timeOfCreation = jsonObject.get("timeOfCreation").getAsLong();

			return new War(declareId, receiveId, timeOfCreation);
		}

		@Override
		public JsonElement serialize(War src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();

			jsonObject.addProperty("declareId", src.getDeclaringTeamId());
			jsonObject.addProperty("receiveId", src.getReceivingTeamId());
			jsonObject.addProperty("timeOfCreation", src.getTimeOfCreation());

			return jsonObject;
		}
	}
}