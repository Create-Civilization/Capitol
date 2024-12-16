package com.createcivilization.capitol.util;

import java.io.*;

public class FileUtils {

	private FileUtils() {}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static File forceFileExistence(File file) throws IOException {
		if (!file.exists()) file.createNewFile();
		file.setWritable(true);
		file.setReadable(true);
		return file;
	}

	public static File getLocalFile(String fileName) {
		return new File(System.getProperty("user.dir"), fileName);
	}
}