package com.createcivilization.capitol.util;

import com.createcivilization.capitol.team.Team;

import java.awt.Color;
import java.io.*;
import java.util.*;

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
        System.out.println(str);
        return Team.TeamBuilder.create()
                .setName("test")
                .setTeamId("alsoATest")
                .addPlayer("owner", new ArrayList<>(List.of(UUID.randomUUID())))
                .setColor(Color.RED)
                .build();
    }
}