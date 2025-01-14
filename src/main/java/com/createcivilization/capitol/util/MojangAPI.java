package com.createcivilization.capitol.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MojangAPI {

	public static String getUsernameFromUUID(String uuid) {
		String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", "");
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();
			if (responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder content = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();

				JsonObject json = JsonParser.parseString(content.toString()).getAsJsonObject();
				return json.get("name").getAsString();
			} else {
				System.out.println("Error: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
