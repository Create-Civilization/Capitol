package com.createcivilization.capitol.util;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.team.*;

import com.google.gson.stream.*;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

import wiiu.mavity.util.ObjectHolder;

import java.awt.Color;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utilities related to teams.
 */
@SuppressWarnings("TypeMayBeWeakened")
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
	 * @return If the {@link PlayerEntity} is in a team or not.
	 */
    public static boolean hasTeam(PlayerEntity player) {
        boolean hasTeam = false;
        for (Team team : loadedTeams) {
            for (var uuid : team.getAllPlayers()) if (uuid.equals(player.getUuid())) hasTeam = true;
            if (hasTeam) break;
        }
        return hasTeam;
    }

	/**
	 * Checks if player owns the team that they're in
	 * @param player The player to check for ownership
	 * @return If the player is the owner of their team
	 */
	public static boolean isTeamOwner(PlayerEntity player) {
		Team team = TeamUtils.getTeam(player).getOrThrow();
		return team.getPlayers().get("owner").stream().anyMatch(player.getUuid()::equals);
	}

	/**
	 * @return A {@link Identifier} object of the dimension the player is in.
	 */
	public static Identifier getPlayerDimension(PlayerEntity player) {
		return player.getWorld().getRegistryKey().getValue();
	}

	/**
	 * @return If the {@link PlayerEntity}'s current position is in a claimed chunk.
	 */
	public static boolean isInClaimedChunk(PlayerEntity player) {
		return isClaimedChunk(player.getWorld().getRegistryKey().getValue(), player.getChunkPos());
	}

	public static boolean isInClaimedChunk(PlayerEntity player, BlockPos pos) {
		return isClaimedChunk(player.getWorld().getRegistryKey().getValue(), player.getWorld().getChunk(pos).getPos());
	}

	/**
	 * @return If the given {@link ChunkPos} is representative of the location of a claimed chunk.
	 */
	public static boolean isClaimedChunk(Identifier dimension, ChunkPos pos) {
		boolean result = false;
		for (Team team : loadedTeams) {
			var chunks = team.getClaimedChunks().get(dimension);
			if (chunks != null && chunks.stream().anyMatch(chunkPos -> chunkPos.equals(pos))) result = true;
		}
		return result;
	}

	/**
	 * @return The {@link Permission} the {@link PlayerEntity} has in the chunk they are currently in.
	 */
	public static Permission getPermissionInCurrentChunk(PlayerEntity player) {
		return getPermissionInChunk(player.getChunkPos(), player);
	}

	/**
	 * @return The {@link Permission} the {@link PlayerEntity} has in the chunk at the {@link BlockPos} specified in the parameters.
	 */
	public static Permission getPermissionInChunk(BlockPos pos, PlayerEntity player) {
		return getPermissionInChunk(player.getWorld().getChunk(pos).getPos(), player);
	}

	/**
	 * @return The {@link Permission} the {@link PlayerEntity} has in the chunk at the {@link ChunkPos} specified in the parameters.
	 */
	public static Permission getPermissionInChunk(ChunkPos pos, PlayerEntity player) {
		return getTeam(pos, player.getWorld().getRegistryKey().getValue())
			.ifPresentOrElse(team -> TeamUtils.getPlayerPermission(team, player), () -> Permission.NONE_REFERENCE); // Prevent null
	}

	/**
	 * @return An {@link ObjectHolder} with a value of either the {@link Team} the given {@link PlayerEntity} is in, or a value of {@code null} if the {@link PlayerEntity} is not in a team.
	 */
    public static ObjectHolder<Team> getTeam(PlayerEntity player) {
        for (Team team : loadedTeams) if (team.getAllPlayers().stream().anyMatch(player.getUuid()::equals)) return new ObjectHolder<>(team);
        return new ObjectHolder<>();
    }

	/**
	 * @return An {@link ObjectHolder} with a value of either the {@link Team} the given {@link String} represents, or a value of {@code null} if no {@link Team} can be found with that id.
	 */
	public static ObjectHolder<Team> getTeam(String teamId) {
		for (Team team : loadedTeams) if (team.getTeamId().equals(teamId)) return new ObjectHolder<>(team);
		return new ObjectHolder<>();
	}

	public static ObjectHolder<Team> getTeam(ChunkPos pos, Identifier dimension) {
		for (Team team : loadedTeams) {
			List<ChunkPos> chunks = team.getClaimedChunks().get(dimension);
			if (chunks != null && chunks.stream().anyMatch(chunkPos -> chunkPos.equals(pos))) return new ObjectHolder<>(team);
		}
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
			TeamUtils.loadChunksForTeams();
		}
    }

	public static boolean isRoleHigher(Team team, String role, String possiblyBiggerRole) {
		for (String currRole : team.getRoleRanking()) {
			if (Objects.equals(currRole, possiblyBiggerRole)) return false;
			else if (Objects.equals(currRole, role)) return true;
		}
		return false;
	}

	/**
	 * Saves all the {@link Team}s to the team data file.
	 */
    public static void saveTeams() throws IOException {
        System.out.println("Saving teams...");
		File teamDataFile = TeamUtils.getTeamDataFile();
        JsonWriter writer = new JsonWriter(new FileWriter(teamDataFile));
        writer.beginArray();
        for (Team team : loadedTeams) team.toString(writer);
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
        Map<String, List<UUID>> players = new LinkedHashMap<>();
		Map<String, Permission> rolePermissions = new HashMap<>();
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
                    while (reader.hasNext()) players.put(reader.nextName(), getListOfUUIDs(reader));
                    reader.endObject();
                }
				case "rolePermissions" -> {
					reader.beginObject();
					while (reader.hasNext()) rolePermissions.put(reader.nextName(), getPermission(reader));
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
				.setRolePermissions(rolePermissions)
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

	private static Permission getPermission(JsonReader reader) throws IOException {
		List<Boolean> permission = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			permission.add(Objects.equals(reader.nextString(), "true"));
		}
		reader.endArray();
		return PermissionUtil.listToPermission(permission);
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
    public static Team createTeam(String name, PlayerEntity player, Color color) {
        return Team.TeamBuilder.create()
                .setName(name)
                .setTeamId(createRandomTeamId())
                .addPlayer("owner", new ArrayList<>(List.of(player.getUuid())))
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
	public static void loadChunksForTeams() throws IOException {
		var file = TeamUtils.getChunkDataFile();
		JsonReader reader = new JsonReader(new FileReader(file));
		reader.beginArray();
		while (reader.hasNext()) loadChunksForTeam(reader);
		reader.endArray();
		reader.close();
	}

	/**
	 * See {@link #loadChunksForTeams()}
	 */
	@SuppressWarnings("deprecation")
	public static void loadChunksForTeam(JsonReader reader) throws IOException {
		if (Config.debugLogs.getOrThrow()) System.out.println("Loading chunk for team...");
		reader.beginObject();
		ObjectHolder<Team> team = new ObjectHolder<>();
		String[] coords;
		while (reader.hasNext()) {
			switch (reader.nextName()) {
				case "teamId" -> team.setFrom(TeamUtils.getTeam(reader.nextString()));
				case "claimedChunks" -> {
					reader.beginObject();
					while (reader.hasNext()) {
						String dimension = reader.nextName();
						reader.beginArray();
						while (reader.hasNext()) {
							coords = reader.nextString().split(Pattern.quote(","));
							if (Config.debugLogs.getOrThrow()) System.out.println("Loading chunk... " + Arrays.toString(coords));
							final String[] finalCoords = coords;
							final int x = Integer.parseInt(finalCoords[0]);
							final int z = Integer.parseInt(finalCoords[1]);
							if (Config.debugLogs.getOrThrow()) System.out.println(Capitol.server.getAsString());
							Capitol.server.ifPresent((server) -> team.ifPresent((t) -> {
								for (var entrySet : server.forgeGetWorldMap().entrySet()) {
									Identifier resourceLoc = new Identifier(dimension);
									if (entrySet.getKey().equals(RegistryKey.of(RegistryKeys.WORLD, resourceLoc))) claimChunk(t, resourceLoc, entrySet.getValue().getChunk(x, z).getPos());
								}
							}));
						}
						reader.endArray();
					}
					reader.endObject();
				}
				// this is an afront to god. -Orion
				// Good thing god (McArctic) isn't watching -Matty
				case "chunksWithCapitolBlock" -> {
					reader.beginObject();
					while (reader.hasNext()) {
						String dimension = reader.nextName();
						reader.beginArray();
						while (reader.hasNext()) {
							coords = reader.nextString().split(Pattern.quote(","));
							final int x = Integer.parseInt(coords[0]);
							final int z = Integer.parseInt(coords[1]);
							Capitol.server.ifPresent((server) -> team.ifPresent((t) -> {
								for (var entrySet : server.forgeGetWorldMap().entrySet()) {
									Identifier resourceLoc = new Identifier(dimension);
									if (entrySet.getKey().equals(RegistryKey.of(RegistryKeys.WORLD, resourceLoc))) t.addCapitolBlock(resourceLoc, entrySet.getValue().getChunk(x, z).getPos());
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
		JsonWriter writer = new JsonWriter(new FileWriter(TeamUtils.getChunkDataFile()));
		writer.beginArray();
		for (Team team : loadedTeams) {
			writer.beginObject();
			writer.name("teamId").value(team.getTeamId());
			JsonUtils.advancedSaveJsonMapHoldingList(
					writer,
					"claimedChunks",
					team.getClaimedChunks(),
					Identifier::toString, // Transforms the Identifier into namespace:path
					TeamUtils::chunksToStringArray,
					false
			);
			JsonUtils.advancedSaveJsonMapHoldingList(
				writer,
				"chunksWithCapitolBlock",
				team.getCapitolBlocks(),
				Identifier::toString, // Transforms the Identifier into namespace:path
				TeamUtils::chunksToStringArray,
				false
			);
			writer.endObject();
		}
		writer.endArray();
		writer.close();
	}

	public static String[] chunksToStringArray(List<ChunkPos> positions) {
		String[] result = new String[positions.size()];
		for (int i = 0; i < positions.size(); i++) {
			var pos = positions.get(i);
			result[i] = pos.x + "," + pos.z;
		}
		return result;
	}

	/**
	 * Claims the current chunk for the given player's team.
	 * @return 1 if successful, -1 if failed (for /command usage)
	 */
	public static int claimCurrentChunk(PlayerEntity player) {
		return getTeam(player).ifPresentOrElse(team -> claimChunk(team, getPlayerDimension(player), player.getChunkPos()), () -> -1);
	}

	/**
	 * Unclaims the current chunk for the given player's team.
	 * @return 1 if successful, -1 if failed (for /command usage)
	 */
	public static int unclaimCurrentChunk(PlayerEntity player) {
		return getTeam(player).ifPresentOrElse(team -> unclaimChunk(team, getPlayerDimension(player), player.getChunkPos()), () -> -1);
	}

	/**
	 * Check if chunk has CapitolBlock.
	 * @param pos The chunk position.
	 * @param dimension The dimension.
	 * @return boolean
	 */
	public static boolean hasCapitolBlock(ChunkPos pos, Identifier dimension){
		boolean result = false;
		for (Team team : loadedTeams) {
			List<ChunkPos> chunks = team.getClaimedChunks().get(dimension);
			if (chunks != null && chunks.stream().anyMatch(chunkPos -> chunkPos.equals(pos))) result = true;
		}
		return result;
	}

	/**
	 * Checks if nearby chunks in radius are claimed by player's team.
	 * @param player Player to check
	 * @param radius The chunk radius around the player to check
	 */
	public static boolean nearClaimedChunk(ChunkPos chunkPos, int radius, PlayerEntity player) {
		Identifier dimension = player.getWorld().getRegistryKey().getValue();
		radius++;
		for (int x = -1; x < radius; x++) {
			for (int z = -1; z < radius; z++) {
				ChunkPos currentChunkPos = new ChunkPos(chunkPos.x - x, chunkPos.z - z);
				if (allowedInChunk(player, dimension, currentChunkPos)) return true;
				else if (TeamUtils.isClaimedChunk(dimension, chunkPos)) return true;
			}
		}
		return false;
	}

	public static Permission getPlayerPermission(Team team, PlayerEntity player){
		return team.getPermission(team.getPlayerRole(player.getUuid()));
	}

	/**
	 * Check if player's team owns chunk at position.
	 * @param player the player on which the team shall be checked.
	 * @param chunkPos the position of the chunk.
	 */
	public static boolean allowedInChunk(PlayerEntity player, Identifier dimension, ChunkPos chunkPos) {
		ObjectHolder<Team> holder = TeamUtils.getTeam(chunkPos, dimension);
		if (holder.isEmpty()) return false;
		return !Objects.equals(holder.getOrThrow().getPlayerRole(player.getUuid()), "non-member");
	}

	/**
	 * Check if team owns chunk at position.
	 * @param team the team on which the team shall be checked.
	 * @param chunkPos the position of the chunk.
	 */
	public static boolean allowedInChunk(Team team, Identifier dimension, ChunkPos chunkPos)
	{
		if (!TeamUtils.isClaimedChunk(dimension, chunkPos)) return false;
		List<ChunkPos> chunks = team.getClaimedChunks().get(dimension);
		return chunks.contains(chunkPos);
	}

	/**
	 * Claims chunks in a radius of a center position to a team
	 * @param team The team to claim the chunks to
	 * @param dimension The dimension to claim the chunks in
	 * @param chunkPos The center of the radius to claim the chunks in
	 * @param radius The radius itself
	 */
	public static void claimChunkRadius(Team team, Identifier dimension, ChunkPos chunkPos, int radius) {
		radius++;
		for (int x = -1; x < radius; x++) for (int z = -1; z < radius; z++) TeamUtils.claimChunkIfNotClaimed(team, dimension, new ChunkPos(chunkPos.x - x, chunkPos.z - z));
	}

	/**
	 * Unclaims chunks in a radius of a center position from a team
	 * @param team The team to claim the chunks to
	 * @param dimension The dimension to claim the chunks in
	 * @param chunkPos The center of the radius to claim the chunks in
	 * @param radius The radius itself
	 */
	public static void unclaimChunkRadius(Team team, Identifier dimension, ChunkPos chunkPos, int radius) {
		radius++;
		for (int x = -1; x < radius; x++) for (int z = -1; z < radius; z++) TeamUtils.unclaimChunkIfFromTeam(team, dimension, new ChunkPos(chunkPos.x - x, chunkPos.z - z));
	}

	/**
	 * Claims the given chunk for the given team.
	 * @return 1 if successful, -1 if failed (for /command usage)
	 */
	public static int claimChunk(Team team, Identifier dimension, ChunkPos pos) {
		if (Config.debugLogs.getOrThrow()) System.out.println("Claiming chunk " + pos + " in dimension " + dimension + " for team '" + team.getName() + "'");
		List<ChunkPos> claimedChunks = team.getClaimedChunks().get(dimension); // Suck my proper typing
		if (claimedChunks == null) {
			List<ChunkPos> list = new ArrayList<>();
			list.add(pos);
			team.getClaimedChunks().put(dimension, list);
		} else claimedChunks.add(pos);
		return 1;
	}


	/**
	 * Unclaims the given chunk from the given team.
	 * @param team The team to unclaim for.
	 * @param dimension The dimension of the chunk.
	 * @param chunkPos The position of the chunk.
	 * @return 1 if successful, -1 if failed
	 */
	public static int unclaimChunk(Team team, Identifier dimension, ChunkPos chunkPos) {
		if (Config.debugLogs.getOrThrow()) System.out.println("Unclaiming chunk " + chunkPos + " in dimension " + dimension + " from team '" + team.getName() + "'");
		List<ChunkPos> claimedChunks = team.getClaimedChunks().get(dimension);
		claimedChunks.remove(chunkPos);
		return 1;
	}

	public static void claimChunkIfNotClaimed(Team team, Identifier dimension, ChunkPos pos) {
		if (!TeamUtils.isClaimedChunk(dimension, pos)) TeamUtils.claimChunk(team, dimension, pos);
	}

	public static void unclaimChunkIfFromTeam(Team team, Identifier dimension, ChunkPos pos) {
		if (TeamUtils.isClaimedChunk(dimension, pos) && TeamUtils.getTeam(pos, dimension).getOrThrow().equals(team)) TeamUtils.unclaimChunk(team, dimension, pos);
	}

	public static List<Team> getTeamAndAllies(Team team) {
		List<Team> teams = new ArrayList<>();
		teams.add(team);
		team.getAllies().forEach(teamId -> getTeam(teamId).ifPresent(teams::add));
		return teams;
	}
}