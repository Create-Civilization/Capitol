package com.createcivilization.capitol.util;

import com.createcivilization.capitol.team.Team;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;

public class TeamUtils {

    private TeamUtils() { throw new AssertionError(); }

    public static File getTeamDataFile() {
        return new File(System.getProperty("user.dir"), "team_data.json");
    }

    public static Team parseTeam(String str) {
        return new Team("", "", new HashMap<>(), Color.WHITE);
    }
}