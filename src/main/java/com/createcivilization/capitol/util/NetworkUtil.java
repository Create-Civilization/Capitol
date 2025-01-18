package com.createcivilization.capitol.util;

import java.io.*;
import java.net.*;

import com.google.gson.*;

public class NetworkUtil {

	public static final int HTTP_STATUS_OK = 200;

	public static String errorRequest(String url) {
		return "An error occurred during a request to url '" + url + "'";
	}

	public static String getUsernameFromUUID(String uuid) {
		String content = requestResponseFromUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", ""));
		JsonObject json = JsonParser.parseString(content).getAsJsonObject();
		return json.get("name").getAsString();
	}

	public static String requestResponseFromUrl(String url) {
		return requestResponseFromUrl(url, "GET");
	}

	public static String requestResponseFromUrl(String url, String requestMethod) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod(requestMethod);
			int responseCode = connection.getResponseCode();
			if (responseCode == HTTP_STATUS_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder content = new StringBuilder();
				while ((inputLine = in.readLine()) != null) content.append(inputLine);
				in.close();
				return content.toString();
			} else System.out.println(errorRequest(url) + ", expected response code: " + HTTP_STATUS_OK + ", got: " + responseCode);
		} catch (Exception e) {
			String msg = errorRequest(url);
			System.out.println(msg);
			System.err.println(msg);
			e.printStackTrace(System.out);
			e.printStackTrace(System.err);
		}
		return null;
	}
}