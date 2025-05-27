package com.createcivilization.capitol.util.data;

import wiiu.mavity.wiiu_lib.util.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileUtil {

	public static File forceFileFromString(String string) throws IOException {
		return FileUtils.forceFileExistence(FileUtils.getLocalFile(string));
	}

	/**
	 * @return The {@link File} which stores receivingTeam data, automatically created if it doesn't exist.
	 */
    public static File getTeamDataFile() throws IOException {
		return forceFileFromString("team_data.json");
    }

	/**
	 * @return The {@link File} which stores war data, automatically created if it doesn't exist.
	 */
	public static File getWarDataFile() throws IOException {
		return forceFileFromString("war_data.json");
	}
}
