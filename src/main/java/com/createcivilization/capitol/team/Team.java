package com.createcivilization.capitol.team;

import com.google.gson.stream.JsonWriter;

import java.awt.Color;
import java.io.*;
import java.util.*;

public class Team {

    private final String name, teamId;

    private final Map<String, UUID> players;

    private final Color colour;

    public Team(String name, String teamId, Map<String, UUID> players, Color colour) {
        this.name = name;
        this.teamId = teamId;
        this.players = players;
        this.colour = colour;
    }

    public Color getColour() {
        return colour;
    }

    public Map<String, UUID> getPlayers() {
        return players;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        JsonWriter writer = new JsonWriter(new StringWriter());

        try {
            writer.beginObject();
            writer.name("name").value(name);
            writer.name("teamId").value(teamId);
            writer.name("colour").value(colour.getRGB());
            writer.name("players").beginObject();
            for (Map.Entry<String, UUID> entry : players.entrySet()) {
                writer.name(entry.getKey()).value(entry.getValue().toString());
            }
            writer.endObject();
            writer.endObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write team data!", e);
        }

        try {
            return writer.getClass().getField("out").get(writer).toString();
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException("That's not even possible, bruh how'd you do that", e);
        }
    }

    public static class TeamBuilder {

    }
}