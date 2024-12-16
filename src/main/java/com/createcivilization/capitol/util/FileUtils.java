package com.createcivilization.capitol.util;

import java.io.*;
import java.util.StringJoiner;

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

	public static String getFileContents(File file) throws IOException {
		var reader = new BufferedReader(new FileReader(file));
		StringJoiner sj = new StringJoiner("\n");
		reader.lines().forEach(sj::add);
		reader.close();
		return sj.toString();
	}

	public static void setContentsIfEmpty(File file, String newContents) throws IOException {
		String contents = getFileContents(file);
		if (contents.isBlank() || contents.isEmpty()) {
			var f = new FileWriter(file);
			f.write(newContents);
			f.close();
		}
	}
}