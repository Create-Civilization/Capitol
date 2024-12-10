package com.createcivilization.capitol.util;

import com.createcivilization.capitol.team.Team;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.awt.Color;
import java.io.*;
import java.util.*;

public class TeamUtils {

    private TeamUtils() { throw new AssertionError(); }

    public static final List<Team> loadedTeams = new ArrayList<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static File getTeamDataFile() throws IOException {
        var file = new File(System.getProperty("user.dir"), "team_data.json");
        if (!file.exists()) file.createNewFile();
        file.setWritable(true);
        file.setReadable(true);
        return file;
    }

    public static void loadTeams() throws IOException {
        System.out.println("Loading teams...");
        var file = TeamUtils.getTeamDataFile();
        var reader = new BufferedReader(new FileReader(file));
        StringJoiner sj = new StringJoiner("\n");
        reader.lines().forEach(sj::add);
        reader.close();
        String json = sj.toString();
        if (json.isBlank() || json.isEmpty()) {
            try {
                var f = new FileWriter(file);
                f.write(
                        "[" +
                        "\n" +
                        "]"
                );
                f.close();
                reader = new BufferedReader(new FileReader(file));
                sj = new StringJoiner("\n");
                reader.lines().forEach(sj::add);
                reader.close();
                json = sj.toString();
            } finally {
                System.out.println("Loading file for first time!");
                loadedTeams.addAll(parseTeams(json));
            }
        } else {
            System.out.println("Loading file with previous team data!");
            loadedTeams.addAll(parseTeams(json));
        }
    }

    public static void saveTeams() throws IOException {
        System.out.println("Saving teams...");
        JsonWriter writer = new JsonWriter(new FileWriter(TeamUtils.getTeamDataFile()));
        writer.setIndent("    ");
        writer.beginArray();
        for (Team team : loadedTeams) writer.jsonValue(team.toString());
        writer.endArray();
        writer.close();
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
                case "name" -> name = reader.nextString();
                case "teamId" -> teamId = reader.nextString();
                case "color" -> color = new Color(reader.nextInt());
                case "players" -> {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        switch (reader.nextName()) {
                            case "owner" -> players.put("owner", getListOfUUIDs(reader));
                            case "mod" -> players.put("mod", getListOfUUIDs(reader));
                            case "member" -> players.put("member", getListOfUUIDs(reader));
                        }
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
        reader.beginArray();
        while (reader.hasNext()) UUIDs.add(UUID.fromString(reader.nextString()));
        reader.endArray();
        return UUIDs;
    }

    public static Team parseTeam(String str) throws IOException {
        return parseTeam(new JsonReader(new StringReader(str)));
    }
}