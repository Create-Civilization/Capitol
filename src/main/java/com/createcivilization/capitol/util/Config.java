package com.createcivilization.capitol.util;

import com.google.gson.stream.*;

import wiiu.mavity.util.ObjectHolder;

import java.io.*;

public class Config {

	private Config() { throw new AssertionError("java.lang.reflect is cheating!"); }

	public static final ObjectHolder<Integer> claimRadius = new ObjectHolder<>(1);
	public static final ObjectHolder<Boolean> debugLogs = new ObjectHolder<>(false);
	public static final ObjectHolder<Integer> inviteTimeout = new ObjectHolder<>(120);

	public static void loadConfig() throws IOException {
		System.out.println("Loading config...");
		File file = FileUtils.forceFileExistence(FileUtils.getLocalFile("config", "capitol_server.json"));
		FileUtils.setContentsIfEmpty(file, "{\"claimRadius\": 1, \"debugLogs\": false, \"inviteTimeout\": 120}");
		JsonReader configReader = new JsonReader(new StringReader(FileUtils.getFileContents(file)));
		configReader.beginObject();
		while (configReader.hasNext()) {
			switch (configReader.nextName()) {
				case "claimRadius" -> claimRadius.set(configReader.nextInt());
				case "debugLogs" -> debugLogs.set(configReader.nextBoolean());
				case "inviteTimeout" -> inviteTimeout.set(configReader.nextInt());
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
		writer.name("claimRadius").value(claimRadius.getOrThrow());
		writer.name("debugLogs").value(debugLogs.getOrThrow());
		writer.name("inviteTimeout").value(inviteTimeout.getOrThrow());
		writer.endObject();
		writer.close();
		System.out.println("Config saved!");
	}
}