package com.createcivilization.capitol.team;

import com.google.gson.stream.JsonWriter;

import java.awt.Color;
import java.io.*;
import java.util.*;

@SuppressWarnings("all")
public class Team {

    private final String name, teamId;

    private final Map<String, List<UUID>> players;

    private final Color color;

    private Team(String name, String teamId, Map<String, List<UUID>> players, Color colour) {
        this.name = name;
        this.teamId = teamId;
        this.players = players;
        this.color = colour;
    }

    public Color getColor() {
        return color;
    }

    public Map<String, List<UUID>> getPlayers() {
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
        writer.setIndent("    ");

        try {
            writer.beginObject();
            writer.name("name").value(name);
            writer.name("teamId").value(teamId);
            writer.name("color").value(color.getRGB());
            writer.name("players").beginObject();
            for (Map.Entry<String, List<UUID>> entry : players.entrySet()) {
                writer.name(entry.getKey()).beginArray();
                for (UUID uuid : entry.getValue()) writer.value(uuid.toString());
                writer.endArray();
            }
            writer.endObject();
            writer.endObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write team data!", e);
        }

        try {
            var field = writer.getClass().getDeclaredField("out");
            field.trySetAccessible();
            return field.get(writer).toString();
        } catch (Throwable e) {
            throw new RuntimeException("That's not even possible, bruh how'd you do that", e);
        }
    }

    public static enum TeamPermssion {
        OWNER(true, true, true, true, true),
        MODERATOR(true, true, true, false, true),
        MEMBER(true, true, true, false, false);

        private final boolean
                canBreakBlocks,
                canUseBlocks,
                canUseItems,
                canManageTeam,
                canManageMembers;

        private TeamPermssion(boolean canBreakBlocks, boolean canUseBlocks, boolean canUseItems, boolean canManageTeam, boolean canManageMembers) {
            this.canBreakBlocks = canBreakBlocks;
            this.canUseBlocks = canUseBlocks;
            this.canUseItems = canUseItems;
            this.canManageTeam = canManageTeam;
            this.canManageMembers = canManageMembers;
        }
    }

    public static class TeamBuilder {

        private String name, teamId;

        private Map<String, List<UUID>> players = new HashMap<>();

        private Color color;

        private TeamBuilder() {}

        public static TeamBuilder create() {
            return new TeamBuilder();
        }

        public TeamBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public TeamBuilder setTeamId(String teamId) {
            this.teamId = teamId;
            return this;
        }

        public TeamBuilder setColor(Color color) {
            this.color = color;
            return this;
        }

        public TeamBuilder addPlayer(String permissionLevel, List<UUID> players) {
            var alreadyAdded = this.players.get(permissionLevel);
            if (alreadyAdded != null) alreadyAdded.addAll(players);
            else this.players.put(permissionLevel, players);
            return this;
        }

        public TeamBuilder setPlayers(Map<String, List<UUID>> players) {
            this.players = players;
            return this;
        }

        public TeamBuilder addPlayers(Map<String, List<UUID>> players) {
            for (Map.Entry<String, List<UUID>> entry : players.entrySet()) addPlayer(entry.getKey(), entry.getValue());
            return this;
        }

        public Team build() {
            Objects.requireNonNull(name);
            Objects.requireNonNull(teamId);
            Objects.requireNonNull(players);
            Objects.requireNonNull(color);
            return new Team(name, teamId, players, color);
        }
    }
}