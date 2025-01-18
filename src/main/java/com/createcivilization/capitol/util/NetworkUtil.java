package com.createcivilization.capitol.util;

import com.google.gson.*;

import java.io.*;
import java.net.*;

public class NetworkUtil {

	public static final int HTTP_STATUS_OK = 200;

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
			URL URL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) URL.openConnection();
			connection.setRequestMethod(requestMethod);
			int responseCode = connection.getResponseCode();
			if (responseCode == HTTP_STATUS_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder content = new StringBuilder();
				while ((inputLine = in.readLine()) != null) content.append(inputLine);
				in.close();
				connection.disconnect();
				return content.toString();
			} else throw new NetworkException(URL + ", expected response code: " + HTTP_STATUS_OK + ", got: " + responseCode);
		} catch (Exception e) {
			throw new NetworkException(NetworkException.errorRequest(url), e);
		}
	}

	public static class NetworkException extends RuntimeException {

		public static String errorRequest(String url) {
			return "An error occurred during a request to url '" + url + "'";
		}

		public NetworkException(String message) {
			super(message);
		}

		public NetworkException(URL url) {
			this(errorRequest(url.toString()));
		}

		public NetworkException(URL url, Throwable cause) {
			this(errorRequest(url.toString()), cause);
		}

		public NetworkException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}