package com.createcivilization.capitol.util;

import com.createcivilization.capitol.team.Team;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;

import java.awt.Color;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

public class GsonUtil {
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapter(Color.class, new ColorAdapter())
		.registerTypeAdapter(UUID.class, new UUIDAdapter())
		.registerTypeAdapter(ResourceLocation.class, new ResourceLocationAdapter())
		.registerTypeAdapter(Team.TeamDimensionData.class, new TeamDimensionDataAdapter())
		.create();

	public static String serialize(Team team) {
		return GSON.toJson(team);
	}

	public static String serializeList(List<Team> teams) {
		return GSON.toJson(teams);
	}

	public static Team deserialize(String json) {
		return GSON.fromJson(json, Team.class);
	}

	public static List<Team> deserializeList(String json) {
		Type listType = new TypeToken<List<Team>>() {}.getType();
		return GSON.fromJson(json, listType);
	}

	public static void saveToFile(List<Team> teams, String filePath) throws IOException {
		try (FileWriter writer = new FileWriter(filePath)) {
			GSON.toJson(teams, writer);
		}
	}

	public static List<Team> loadFromFile(String filePath) throws IOException {
		try (FileReader reader = new FileReader(filePath)) {
			Type listType = new TypeToken<List<Team>>() {}.getType();
			return GSON.fromJson(reader, listType);
		}
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
			return new ResourceLocation(json.getAsString());
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

}
