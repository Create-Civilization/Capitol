package com.createcivilization.capitol.util;

import com.google.gson.stream.*;

import wiiu.mavity.util.ObjectHolder;

import java.io.*;

public class Config {

	private Config() { throw new AssertionError("java.lang.reflect is cheating!"); }

	public static final ObjectHolder<Integer> claimRadius = new ObjectHolder<>(1);
	public static final ObjectHolder<Boolean> debugLogs = new ObjectHolder<>(false);

	public static void loadConfig() throws IOException {
		System.out.println("Loading config...");
		File file = FileUtils.forceFileExistence(FileUtils.getLocalFile("config", "capitol_server.json"));
		FileUtils.setContentsIfEmpty(file, "{\"claimRadius\": 1, \"debugLogs\": false}");
		JsonReader configReader = new JsonReader(new StringReader(FileUtils.getFileContents(file)));
		configReader.beginObject();
		while (configReader.hasNext()) {
			switch (configReader.nextName()) {
				case "claimRadius" -> claimRadius.set(configReader.nextInt());
				case "debugLogs" -> debugLogs.set(configReader.nextBoolean());
			}
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