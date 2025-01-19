package wiiu.mavity.util.network;

import com.google.gson.*;

import com.mojang.util.UUIDTypeAdapter;

import java.io.*;
import java.net.*;
import java.util.UUID;

public class NetworkUtil {

	public static String getUsernameFromUUID(UUID uuid) {
		String content = requestResponseFromUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + UUIDTypeAdapter.fromUUID(uuid));
		JsonObject json = JsonParser.parseString(content).getAsJsonObject();
		return json.get("name").getAsString();
	}

	public static String requestResponseFromUrl(String targetURL) {
		return requestResponseFromUrl(targetURL, "GET");
	}

	public static String requestResponseFromUrl(String targetURL, String requestMethod) {
		URL url = url(targetURL);
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			int responseCode = connection.getResponseCode();
			if (HttpResponseCode.get(responseCode) == HttpResponseCode.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuilder content = new StringBuilder();
				while ((inputLine = in.readLine()) != null) content.append(inputLine);
				in.close();
				connection.disconnect();
				return content.toString();
			} else throw new NetworkException(url + ", expected response code: " + HttpResponseCode.HTTP_OK + ", got: " + responseCode);
		} catch (Exception e) {
			throw new NetworkException(url, e);
		}
	}

	public static URL url(String targetURL) {
		try {
			return new URL(targetURL);
		} catch (MalformedURLException e) {
			throw new NetworkException("Invalid or malformed url: '" + targetURL + "'", e);
		}
	}
}