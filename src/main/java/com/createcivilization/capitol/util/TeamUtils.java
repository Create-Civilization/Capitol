package com.createcivilization.capitol.util;

import com.createcivilization.capitol.team.Team;

import java.io.*;

public class TeamUtils {

    private TeamUtils() { throw new AssertionError(); }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTeamDataFile() throws IOException {
        var file = new File(System.getProperty("user.dir"), "team_data.json");
        if (!file.exists()) file.createNewFile();
        file.setWritable(true);
        file.setReadable(true);
        return file;
    }

    public static Team parseTeam(String str) {

        return Team.TeamBuilder.create().build();
    }
}