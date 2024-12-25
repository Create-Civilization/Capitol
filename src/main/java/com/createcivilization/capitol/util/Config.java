package com.createcivilization.capitol.util;

import com.google.gson.stream.*;

import wiiu.mavity.util.ObjectHolder;

import java.io.*;

public class Config {

	private Config() { throw new AssertionError("java.lang.reflect is cheating!"); }

	public static final ObjectHolder<Integer> claimRadius = new ObjectHolder<>();

	public static void loadConfig() throws IOException {
		System.out.println("Loading config...");
		JsonReader configReader = new JsonReader(new StringReader(FileUtils.getFileContents(FileUtils.forceFileExistence(FileUtils.getLocalFile("config", "capitol_server.json")))));
		configReader.beginObject();
		while (configReader.hasNext()) {
			String key = configReader.nextName();
			if (key.equals("claimRadius")) claimRadius.set(configReader.nextInt());
		}
		configReader.endObject();
		configReader.close();
		System.out.println("Config loaded!");
	}

	public static void saveConfig() throws IOException {
		System.out.println("Saving config...");
		JsonWriter writer = new JsonWriter(new FileWriter(FileUtils.forceFileExistence(FileUtils.getLocalFile("config", "capitol_server.json"))));
		writer.beginObject();
		writer.name("claimRadius").value(claimRadius.get());
		writer.endObject();
		writer.close();
		System.out.println("Config saved!");
	}
}