package com.createcivilization.capitol.util;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.team.Team;

import com.google.gson.stream.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;

import wiiu.mavity.util.ObjectHolder;

import java.awt.Color;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

public class TeamUtils {

    private TeamUtils() { throw new AssertionError(); }

    public static final List<Team> loadedTeams = new ArrayList<>();

    public static File getTeamDataFile() throws IOException {
		return FileUtils.forceFileExistence(FileUtils.getLocalFile("team_data.json"));
    }

	public static File getChunkDataFile() throws IOException {
		return FileUtils.forceFileExistence(FileUtils.getLocalFile("claimed_chunks.json"));
	}

    public static boolean hasTeam(Player player) {
        boolean hasTeam = false;
        for (Team team : loadedTeams) {
            for (var UUIDs : team.getAllPlayers()) if (UUIDs.contains(player.getUUID())) hasTeam = true;
            if (hasTeam) break;
        }
        return hasTeam;
    }

	@SuppressWarnings("resource")
	public static ResourceLocation getPlayerDimension(Player player) {
		return player.level().dimension().location();
	}

	public static boolean isInClaimedChunk(Player player) {
		for (Team team : loadedTeams) {
			for (var claimedChunks : team.getClaimedChunks().values()) {
				if (claimedChunks.contains(player.chunkPosition())) return true;
			}
		}
		return false;
	}

    public static ObjectHolder<Team> getTeam(Player player) {
        for (Team team : loadedTeams) for (var UUIDs : team.getAllPlayers()) if (UUIDs.contains(player.getUUID())) return new ObjectHolder<>(team);
        return new ObjectHolder<>();
    }

	public static ObjectHolder<Team> getTeam(String teamId) {
		for (Team team : loadedTeams) if (team.getTeamId().equals(teamId)) return new ObjectHolder<>(team);
		return new ObjectHolder<>();
	}

    public static void loadTeams() throws IOException {
        System.out.println("Loading teams...");
        var file = TeamUtils.getTeamDataFile();
		try {
			FileUtils.setContentsIfEmpty(file, "[\n]");
		} finally {
			loadedTeams.addAll(parseTeams(FileUtils.getFileContents(file)));
			TeamUtils.loadChunks();
		}
    }

    public static void saveTeams() throws IOException {
        System.out.println("Saving teams...");
        JsonWriter writer = new JsonWriter(new FileWriter(TeamUtils.getTeamDataFile()));
        writer.beginArray();
        for (Team team : loadedTeams) writer.jsonValue(team.toString());
        writer.endArray();
        writer.close();
		TeamUtils.saveChunks();
    }

    public static List<Team> parseTeams(String str) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(str));
        List<Team> teams = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) teams.add(parseTeam(reader));
        reader.endArray();
		reader.close();
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

    public static boolean teamExists(String teamName) {
        boolean exists = false;
        for (Team team : loadedTeams) {
            if (team.getName().equalsIgnoreCase(teamName)) {
                exists = true;
                break;
            }
        }
        return exists;
    }

    public static String createRandomTeamId() {
		LocalDateTime time = LocalDateTime.now();
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		sb.append("team_");
		sb.append(UUID.randomUUID().toString().substring(4));
		sb.append(String.valueOf(random.nextBoolean()), 1, 4);
		sb.append(time.getHour() / 13 + time.getNano());
		sb.append(Month.values()[random.nextInt(0, Month.values().length - 1)].toString(), 0, 2);
		sb.append(random.nextInt(1000, 9999));
		sb.append(UUID.randomUUID().toString().substring(7));
		sb.append(time.getDayOfYear());
		if (random.nextBoolean()) sb.append(UUID.randomUUID().toString(), 3, 5);
		else sb.append(time.getDayOfMonth());
		return sb.toString();
    }

    public static Team createTeam(String name, Player player, Color color) {
        return Team.TeamBuilder.create()
                .setName(name)
                .setTeamId(createRandomTeamId())
                .addPlayer("owner", new ArrayList<>(List.of(player.getUUID())))
                .setColor(color)
                .build();
    }

	public static int reloadTeamsFromFile() {
		try {
			loadedTeams.clear();
			loadTeams();
			return 1;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			e.printStackTrace(System.err);
			return -1;
		}
	}

	public static int reloadTeams() {
		try {
			saveTeams();
			loadedTeams.clear();
			loadTeams();
			return 1;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			e.printStackTrace(System.err);
			return -1;
		}
	}

	public static void loadChunks() throws IOException {
		var file = TeamUtils.getChunkDataFile();
		JsonReader reader = new JsonReader(new FileReader(file));
		reader.beginArray();
		while (reader.hasNext()) loadChunk(reader);
		reader.endArray();
		reader.close();
	}

	@SuppressWarnings("deprecation")
	public static void loadChunk(JsonReader reader) throws IOException {
		System.out.println("Loading chunk...");
		reader.beginObject();
		String teamId = null;
		String[] coords;
		while (reader.hasNext()) {
			switch (reader.nextName()) {
				case "teamId" -> teamId = reader.nextString();
				case "claimedChunks" -> {
					System.out.println("Loading chunk... *" + teamId);
					reader.beginObject();
					while (reader.hasNext()) {
						String dimension = reader.nextName();
						reader.beginArray();
						while (reader.hasNext()) {
							coords = reader.nextString().split(Pattern.quote(","));
							System.out.println("Loading chunk... **" + Arrays.toString(coords));
							final String finalTeamId = teamId;
							final String[] finalCoords = coords;
							final int x = Integer.parseInt(finalCoords[0]);
							final int z = Integer.parseInt(finalCoords[1]);
							System.out.println(Capitol.server.getAsString());
							Capitol.server.ifPresent((server) -> TeamUtils.getTeam(finalTeamId).ifPresent((team) -> {
								for (var entrySet : server.forgeGetWorldMap().entrySet()) {
									var resourceLoc = new ResourceLocation(dimension);
									if (entrySet.getKey().equals(ResourceKey.create(Registries.DIMENSION, resourceLoc))) claimChunk(team, resourceLoc, entrySet.getValue().getChunk(x, z).getPos());
								}
							}));
						}
						reader.endArray();
					}
					reader.endObject();
				}
			}
		}
		reader.endObject();
	}

	public static void saveChunks() throws IOException {
		var file = TeamUtils.getChunkDataFile();
		JsonWriter writer = new JsonWriter(new FileWriter(file));
		writer.beginArray();
		for (Team team : loadedTeams) {
			writer.beginObject();
			writer.name("teamId").value(team.getTeamId());
			writer.name("claimedChunks").beginObject();
			for (var entrySet : team.getClaimedChunks().entrySet()) {
				writer.name(entrySet.getKey().toString()).beginArray();
				for (var chunks : entrySet.getValue()) writer.value(chunks.x + "," + chunks.z);
				writer.endArray();
			}
			writer.endObject();
			writer.endObject();
		}
		writer.endArray();
		writer.close();
	}

	public static int claimCurrentChunk(Player player) {
		return getTeam(player).ifPresentOrElse(team -> claimChunk(team, getPlayerDimension(player), player.chunkPosition()), () -> -1);
	}

	public static int claimChunk(Team team, ResourceLocation dimension, ChunkPos pos) {
		System.out.println("Claiming chunk " + pos + " in dimension " + dimension + " for team '" + team.getName() + "'");
		var claimedChunks = team.getClaimedChunks().get(dimension);
		if (claimedChunks == null) {
			List<ChunkPos> list = new ArrayList<>();
			list.add(pos);
			team.getClaimedChunks().put(dimension, list);
		} else claimedChunks.add(pos);
		return 1;
	}
}