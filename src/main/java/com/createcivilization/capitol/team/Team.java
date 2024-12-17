package com.createcivilization.capitol.team;

import com.google.gson.stream.JsonWriter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.awt.Color;
import java.io.*;
import java.util.*;

@SuppressWarnings("all")
public class Team {

    private String name, teamId;

    private Map<String, List<UUID>> players;

    private Color color;

	private Map<ResourceLocation, List<ChunkPos>> claimedChunks = new HashMap<>();

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

	public Collection<List<UUID>> getAllPlayers() {
		return players.values();
	}

	public Map<ResourceLocation, List<ChunkPos>> getClaimedChunks() {
		return claimedChunks;
	}

    @Override
    public String toString() {
        JsonWriter writer = new JsonWriter(new StringWriter());

        try {
            writer.beginObject();
            writer.name("name").value(name);
            writer.name("teamId").value(teamId);
            writer.name("color").value(color.getRGB());
            writer.name("players").beginObject();
            for (var entry : players.entrySet()) {
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