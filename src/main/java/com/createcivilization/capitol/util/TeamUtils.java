package com.createcivilization.capitol.util;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.team.*;

import com.google.gson.stream.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;

import wiiu.mavity.util.ObjectHolder;

import javax.annotation.Nullable;
import java.awt.Color;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilities related to teams.
 */
public class TeamUtils {

	/**
	 * Not to be instanced.
	 */
    private TeamUtils() { throw new AssertionError(); }

	/**
	 * A list of the currently loaded teams.
	 */
    public static final List<Team> loadedTeams = new ArrayList<>();

	public static final List<War> wars = new ArrayList<>();

	/**
	 * @return The {@link File} which stores team data, automatically created if it doesn't exist.
	 */
    public static File getTeamDataFile() throws IOException {
		return FileUtils.forceFileExistence(FileUtils.getLocalFile("team_data.json"));
    }

	/**
	 * @return The {@link File} which stores claimed chunk data, automatically created if it doesn't exist.
	 */
	public static File getChunkDataFile() throws IOException {
		return FileUtils.forceFileExistence(FileUtils.getLocalFile("claimed_chunks.json"));
	}

	/**
	 * @return If the {@link Player} is in a team or not.
	 */
    public static boolean hasTeam(Player player) {
        boolean hasTeam = false;
        for (Team team : loadedTeams) {
            for (var uuid : team.getAllPlayers()) if (uuid.equals(player.getUUID())) hasTeam = true;
            if (hasTeam) break;
        }
        return hasTeam;
    }

	/**
	 * Checks if player owns the team that they're in
	 * @param player The player to check for ownership
	 * @return If the player is the owner of his team
	 */
	public static boolean isTeamOwner(Player player) {
		Team team = TeamUtils.getTeam(player).get();

		assert team != null;
		return team.getPlayers().get("owner").stream().anyMatch(player.getUUID()::equals);
	}

	/**
	 * @return A {@link ResourceLocation} object of the dimension the player is in.
	 */
	@SuppressWarnings("resource")
	public static ResourceLocation getPlayerDimension(Player player) {
		return player.level().dimension().location();
	}

	/**
	 * @return If the {@link Player}'s current position is in a claimed chunk.
	 */
	public static boolean isInClaimedChunk(Player player) {
		return isClaimedChunk(player.chunkPosition());
	}

	/**
	 * @return If the given {@link ChunkPos} is representative of the location of a claimed chunk.
	 */
	public static boolean isClaimedChunk(ChunkPos pos) {
		for (Team team : loadedTeams) for (var claimedChunks : team.getClaimedChunks().values()) if (claimedChunks.contains(pos)) return true;
		return false;
	}

	/**
	 * @return The {@link Permission} the {@link Player} has in the chunk they are currently in.
	 */
	public static Permission getPermissionInCurrentChunk(Player player) {
		return getPermissionInChunk(player.chunkPosition(), player);
	}

	/**
	 * @return The {@link Permission} the {@link Player} has in the chunk at the {@link BlockPos} specified in the parameters.
	 */
	@SuppressWarnings("resource")
	public static Permission getPermissionInChunk(BlockPos pos, Player player) {
		return getPermissionInChunk(player.level().getChunkAt(pos).getPos(), player);
	}

	/**
	 * @return The {@link Permission} the {@link Player} has in the chunk at the {@link ChunkPos} specified in the parameters.
	 */
	public static Permission getPermissionInChunk(ChunkPos pos, Player player) {
		boolean isInClaimedChunk = false;
		Team teamWhoClaimedChunk = null;
		for (Team team : loadedTeams) {
			for (var claimedChunks : team.getClaimedChunks().values()) {
				if (claimedChunks.contains(pos)) {
					isInClaimedChunk = true;
					teamWhoClaimedChunk = team;
					break;
				}
			}
		}

		if (isInClaimedChunk) {
			if (teamWhoClaimedChunk.getName().equals("Server")) return Permission.NON_TEAM_MEMBER_ON_SERVER_CLAIM;
			else return teamWhoClaimedChunk.getAllPlayers().stream().anyMatch(player.getUUID()::equals) ? Permission.TEAM_MEMBER_ON_TEAM_CLAIM : Permission.NON_TEAM_MEMBER_ON_TEAM_CLAIM;
		} else return Permission.TEAM_MEMBER_ON_TEAM_CLAIM;
	}

	/**
	 * @return An {@link ObjectHolder} with a value of either the {@link Team} the given {@link Player} is in, or a value of {@code null} if the {@link Player} is not in a team.
	 */
    public static ObjectHolder<Team> getTeam(Player player) {
        for (Team team : loadedTeams) if (team.getAllPlayers().stream().anyMatch(player.getUUID()::equals)) return new ObjectHolder<>(team);
        return new ObjectHolder<>();
    }

	/**
	 * @return An {@link ObjectHolder} with a value of either the {@link Team} the given {@link String} represents, or a value of {@code null} if no {@link Team} can be found with that id.
	 */
	public static ObjectHolder<Team> getTeam(String teamId) {
		for (Team team : loadedTeams) if (team.getTeamId().equals(teamId)) return new ObjectHolder<>(team);
		return new ObjectHolder<>();
	}

	/**
	 * Loads all the {@link Team}s from the team data file.
	 */
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

	/**
	 * Saves all the {@link Team}s to the team data file.
	 */
    public static void saveTeams() throws IOException {
        System.out.println("Saving teams...");
        JsonWriter writer = new JsonWriter(new FileWriter(TeamUtils.getTeamDataFile()));
        writer.beginArray();
        for (Team team : loadedTeams) writer.jsonValue(team.toString());
        writer.endArray();
        writer.close();
		TeamUtils.saveChunks();
    }

	/**
	 * @return A list of {@link Team}s parsed from the given {@link String}.
	 */
    public static List<Team> parseTeams(String str) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(str));
        List<Team> teams = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) teams.add(parseTeam(reader));
        reader.endArray();
		reader.close();
        return teams;
    }

	/**
	 * @return An individual {@link Team} object parsed from json.
	 */
    public static Team parseTeam(JsonReader reader) throws IOException {
        String name = null, teamId = null;
        Map<String, List<UUID>> players = new HashMap<>();
        Color color = null;
		List<String> allies = new ArrayList<>();
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
                            case "moderator" -> players.put("moderator", getListOfUUIDs(reader));
                            case "member" -> players.put("member", getListOfUUIDs(reader));
                        }
                    }
                    reader.endObject();
                }
				case "allies" -> {
					reader.beginArray();
					while (reader.hasNext()) allies.add(reader.nextString());
					reader.endArray();
				}
            }
        }
        reader.endObject();
        return Team.TeamBuilder.create()
                .setName(name)
                .setTeamId(teamId)
                .setPlayers(players)
                .setColor(color)
				.setAllies(allies)
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

	/**
	 * @return A random id for an {@link Team} to use.
	 */
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

	/**
	 * @return A new {@link Team} with the given parameters.
	 */
    public static Team createTeam(String name, Player player, Color color) {
        return Team.TeamBuilder.create()
                .setName(name)
                .setTeamId(createRandomTeamId())
                .addPlayer("owner", new ArrayList<>(List.of(player.getUUID())))
                .setColor(color)
                .build();
    }

	/**
	 * @param teamId The team to delete
	 * @return boolean (Success)
	 */
	public static boolean removeTeam(String teamId) {
		return loadedTeams.removeIf(team -> Objects.equals(team.getTeamId(), teamId));
	}

	/**
	 * Dumps the currently loaded teams, and then loads the teams in the team data file.
	 * @return 1 if successful, -1 if not (for /command usage)
	 */
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

	/**
	 * Saves the teams to the team data file, dumps the team list, then reloads the teams.
	 * @return 1 if successful, -1 if not (for /command usage)
	 */
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

	/**
	 * Loads the claimed chunks from the chunk data file and applies them to the relevant teams.
	 */
	public static void loadChunks() throws IOException {
		var file = TeamUtils.getChunkDataFile();
		JsonReader reader = new JsonReader(new FileReader(file));
		reader.beginArray();
		while (reader.hasNext()) loadChunk(reader);
		reader.endArray();
		reader.close();
	}

	/**
	 * See {@link #loadChunks()}
	 */
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

	/**
	 * Saves the claimed chunks to the chunk data file.
	 */
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

	/**
	 * Claims the current chunk for the given player's team.
	 * @return 1 if successful, -1 if failed (for /command usage)
	 */
	public static int claimCurrentChunk(Player player) {
		return getTeam(player).ifPresentOrElse(team -> claimChunk(team, getPlayerDimension(player), player.chunkPosition()), () -> -1);
	}

	/**
	 * Checks if nearby chunks in radius are claimed by player's team.
	 * @param player Player to check
	 * @param radius The chunk radius around the player to check
	 */
	public static boolean nearClaimedChunk(ChunkPos chunkPos, int radius, @Nullable Player player)
	{
		for (int x = -1; x < radius+1; x++)
		{
			for (int z = -1; z < radius + 1; z++)
			{
				ChunkPos currentChunkPos = new ChunkPos(chunkPos.x-x, chunkPos.z-z);
				if (player != null)
				{
					if (TeamUtils.getPermissionInChunk(currentChunkPos, player) == Permission.TEAM_MEMBER_ON_TEAM_CLAIM) {
						return true;
					}
				}else{
					if (TeamUtils.isClaimedChunk(chunkPos)){
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Claims chunks in a radius of a center position to a team
	 * @param team The team to claim the chunks to
	 * @param dimension The dimension to claim the chunks in
	 * @param chunkPos The center of the radius to claim the chunks in
	 * @param radius The radius itself
	 */
	public static void claimChunkRadius(Team team, ResourceLocation dimension, ChunkPos chunkPos, int radius)
	{
		for (int x = -1; x < radius+1; x++)
		{
			for (int z = -1; z < radius+1; z++)
			{
				ChunkPos currentChunkPos = new ChunkPos(chunkPos.x - x, chunkPos.z - z);
				if (!TeamUtils.isClaimedChunk(currentChunkPos)) {TeamUtils.claimChunk(team, dimension, currentChunkPos);} // Avoid claiming claimed chunks, avoiding overlap
			}
		}
	}

	/**
	 * Claims the given chunk for the given team.
	 * @return 1 if successful, -1 if failed (for /command usage)
	 */
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

	public static List<Team> getTeamAndAllies(Team team) {
		List<Team> teams = new ArrayList<>();
		teams.add(team);
		team.getAllies().forEach(str -> getTeam(str).ifPresent(teams::add));
		return teams;
	}
}