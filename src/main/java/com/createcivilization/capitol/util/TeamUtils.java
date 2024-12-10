package com.createcivilization.capitol.util;

import com.createcivilization.capitol.team.Team;

import com.google.gson.stream.JsonReader;

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

    public static List<Team> parseTeams(String str) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(str));
        List<Team> teams = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) teams.add(parseTeam(reader));
        reader.endArray();
        return teams;
    }

    public static Team parseTeam(JsonReader reader) throws IOException {
        String name = null, teamId = null;
        Map<String, List<UUID>> players = new HashMap<>();
        Color color = null;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "name": name = reader.nextString();
                case "teamId": teamId = reader.nextString();
                case "color": color = new Color(reader.nextInt());
                case "players": {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        var permission = reader.nextName();
                        reader.beginArray();
                        players.put(permission, getListOfUUIDs(reader));
                        reader.endArray();
                    }
                    reader.endObject();
                }
            }
        }
        reader.endObject();
        return Team.TeamBuilder.create()
                .setName(name)
                .setTeamId(teamId)
                .setPlayers(players)
                .setColor(color)
                .build();
    }

    private static List<UUID> getListOfUUIDs(JsonReader reader) throws IOException {
        List<UUID> UUIDs = new ArrayList<>();
        while (reader.hasNext()) UUIDs.add(UUID.fromString(reader.nextString()));
        return UUIDs;
    }

    public static Team parseTeam(String str) throws IOException {
        return parseTeam(new JsonReader(new StringReader(str)));
    }
}