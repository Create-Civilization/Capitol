package com.createcivilization.capitol.util;

import com.google.gson.stream.*;

import wiiu.mavity.util.ObjectHolder;

import java.io.*;
import java.util.StringJoiner;

public class Config {

	private Config() { throw new AssertionError("java.lang.reflect is cheating!"); }

	public static final ObjectHolder<Integer> claimRadius = new ObjectHolder<>(1);
	public static final ObjectHolder<Boolean> debugLogs = new ObjectHolder<>(false);
	public static final ObjectHolder<Integer> inviteTimeout = new ObjectHolder<>(120);

	public static final ObjectHolder<Boolean> nonMemberUseItems = new ObjectHolder<>(true);
	public static final ObjectHolder<Boolean> nonMemberInteractEntities = new ObjectHolder<>(true);
	public static final ObjectHolder<Boolean> nonMemberInteractBlocks = new ObjectHolder<>(true);

	public static final ObjectHolder<Integer> maxChunks = new ObjectHolder<>(1000);
	public static final ObjectHolder<Integer> maxMembers = new ObjectHolder<>(50);

	public static final ObjectHolder<Integer> warTakeoverIncrement = new ObjectHolder<>(1); // TODO: Ask Arctic for the best value
	public static final ObjectHolder<Integer> maxWarTakeoverAmount = new ObjectHolder<>(1000); // TODO: Ask Arctic for the best value

	public static void loadConfig() throws IOException {
		System.out.println("Loading config...");
		File file = FileUtils.forceFileExistence(FileUtils.getLocalFile("config", "capitol_server.json"));
		FileUtils.setContentsIfEmpty(file, Config.generateFileContents());
		JsonReader configReader = new JsonReader(new StringReader(FileUtils.getFileContents(file)));
		configReader.beginObject();
		String key;
		while (configReader.hasNext()) {
			key = configReader.nextName();
			try {
				var field = Config.class.getDeclaredField(key);
				field.setAccessible(true);
				ObjectHolder<?> holder = (ObjectHolder<?>) field.get(null);
				Class<?> type = holder.getType();
				if (type == Integer.TYPE || type == Integer.class) holder.forceSet(configReader.nextInt());
				else if (type == Boolean.TYPE || type == Boolean.class) holder.forceSet(configReader.nextBoolean());
				else if (type == Void.TYPE || type == Void.class) throw new RuntimeException("Field with name '" + key + "' should have a preset value for reflection support!");
				else System.out.println(type.getSimpleName());
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		configReader.endObject();
		configReader.close();
		System.out.println("Config loaded!");
	}

	public static String generateFileContents() {
		String lineSeparator = System.lineSeparator();
		String tab = "    ";
		StringJoiner sj = new StringJoiner("," + lineSeparator + tab, "{" + lineSeparator + tab, lineSeparator + "}");
		for (var field : Config.class.getDeclaredFields()) {
			try {
				field.setAccessible(true);
				sj.add("\"" + field.getName() + "\"" + ": " + ((ObjectHolder<?>) field.get(null)).getAsJsonString());
			} catch (Throwable ignored) {}
		}
		return sj.toString();
	}

	public static void saveConfig() throws IOException {
		System.out.println("Saving config...");
		FileUtils.setFileContents(
			FileUtils.forceFileExistence(FileUtils.getLocalFile("config", "capitol_server.json")), Config.generateFileContents()
		);
		System.out.println("Config saved!");
	}
}