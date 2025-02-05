package com.createcivilization.capitol.util;

import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.team.Team;

import com.google.gson.*;

import wiiu.mavity.wiiu_lib.util.network.NetworkUtil;

import java.time.LocalDateTime;

public class LogToDiscord {

	public static void sendPostRequest(String url, String teamName, String message, String avatarUrl) {

		JsonObject json = new JsonObject();
		json.addProperty("username", teamName);
		json.addProperty("content", message);
		json.addProperty("avatar_url", avatarUrl);

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();

		NetworkUtil.postJsonToUrl(url, gson.toJson(json), false);
	}

	public static void sendPostRequest(String name, String url, String message) {
		String avatarUrl = "https://cdn.discordapp.com/attachments/1301370451942969426/1331829178978406420/SMPLogoRevised.png?ex=67930a14&is=6791b894&hm=8c15e9298df9306c859f21ac815908513f5feda0d1e51300af17c0c791f9839a&";
		sendPostRequest(url, name, message + "; timestamp: " + LocalDateTime.now(), avatarUrl);
	}

	public static void postIfAllowed(String name, String message) {
		if (CapitolConfig.SERVER.logCapitolActions.get()) sendPostRequest(name, CapitolConfig.SERVER.logUrl.get(), message);
	}

	private static void processTeamPosting(Team team, String message) {
		postIfAllowed(team.getName(), message);
	}

	public static void postIfAllowed(Team team, String message) {
		processTeamPosting(team, message);
	}
}