package com.createcivilization.capitol.old.util.team;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.utils.PermissionUtil;
import com.createcivilization.capitol.old.config.CapitolConfig;
import com.createcivilization.capitol.old.payloads.bidirectional.*;
import com.createcivilization.capitol.old.payloads.bidirectional.add.BiAddChunk;
import com.createcivilization.capitol.old.payloads.bidirectional.add.BiAddTeam;
import com.createcivilization.capitol.old.payloads.bidirectional.remove.BiRemoveChunk;
import com.createcivilization.capitol.old.payloads.bidirectional.remove.BiRemoveWar;
import com.createcivilization.capitol.old.payloads.toclient.syncing.*;
import com.createcivilization.capitol.old.team.*;

import com.createcivilization.capitol.old.util.data.DataManager;
import com.createcivilization.capitol.old.util.data.DistHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;

import org.jetbrains.annotations.Nullable;

import wiiu.mavity.wiiu_lib.util.*;

import java.awt.Color;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

/**
 * Utilities related to teams.
 */
@SuppressWarnings({"resource", "TypeMayBeWeakened"})
public class TeamUtils {

	/**
	 * Not to be instanced.
	 */
    private TeamUtils() { throw new AssertionError(); }

	/**
	 * @return If the {@link Player} is in a receivingTeam or not.
	 */
    public static boolean hasTeam(Player player) {
        return hasTeam(player.getUUID());
    }

	/**
	 * @return If the {@link UUID} is in a receivingTeam or not
	 */
	public static boolean hasTeam(UUID playerUUID) {
		return DataManager.TeamData.loadedTeams.stream().anyMatch(team -> team.getMembers().values().stream().anyMatch(list -> list.contains(playerUUID)));
	}

	/**
	 * Checks if player owns the receivingTeam that they're in
	 * @param player The player to check for ownership
	 * @return If the player is the owner of his receivingTeam
	 */
	public static boolean isTeamOwner(Player player) {
		return TeamUtils.getTeam(player).getOrThrow().getMembers().get("owner").stream().anyMatch(player.getUUID()::equals);
	}

	/**
	 * @return A {@link ResourceLocation} object of the dimension the player is in.
	 */
	public static ResourceLocation getPlayerDimension(Player player) {
		return player.level().dimension().location();
	}

	/**
	 * @return If the {@link Player}'s current position is in a claimed chunk.
	 */
	public static boolean isInClaimedChunk(Player player) {
		return isChildChunk(getPlayerDimension(player), player.chunkPosition());
	}

	public static boolean isInClaimedChunk(Player player, BlockPos pos) {
		Level level = player.level();
		return isChildChunk(level.dimension().location(), level.getChunk(pos).getPos());
	}

	/**
	 * @return If the given {@link ChunkPos} is representative of the location of a claimed chunk.
	 */
	public static boolean isChildChunk(ResourceLocation dimension, ChunkPos pos) {
		return DataManager.TeamData.loadedTeams.stream().anyMatch(team -> team.hasChunkPos(dimension, pos));
	}

	/**
	 * @return The Permission map the {@link Player} has in the chunk they are currently in.
	 */
	public static Map<String, Boolean> getPermissionInCurrentChunk(Player player) {
		return getPermissionInChunk(player.chunkPosition(), player);
	}

	/**
	 * TODO: Completely redo the Permission system and replace it with a c2s synced config per receivingTeam (done in the capitol block?)
	 * @return The Permission map the {@link Player} has in the chunk at the {@link BlockPos} specified in the parameters.
	 */
	public static Map<String, Boolean> getPermissionInChunk(BlockPos pos, Player player) {
		return getPermissionInChunk(new ChunkPos(pos), player);
	}

	/**
	 * @return The Permission map the {@link Player} has in the chunk at the {@link ChunkPos} specified in the parameters.
	 */
	public static Map<String, Boolean> getPermissionInChunk(ChunkPos pos, Player player) {
		return getTeam(pos, player.level().dimension().location())
			.ifPresentOrElse(
				(IfPresentFunction<Team, Map<String, Boolean>>) team -> TeamUtils.getPlayerPermission(team, player),
				() -> PermissionUtil.newPermission("all_true") // Prevent null
			);
	}

	/**
	 * @return An {@link ObjectHolder} with a value of either the {@link Team} the given {@link Player} is in, or a value of {@code null} if the {@link Player} is not in a receivingTeam.
	 */
    public static ObjectHolder<Team> getTeam(Player player) {
        for (Team team : DataManager.TeamData.loadedTeams) if (team.getAllPlayers().stream().anyMatch(player.getUUID()::equals)) return new ObjectHolder<>(team);
        return new ObjectHolder<>();
    }

	/**
	 * @return An {@link ObjectHolder} with a value of either the {@link Team} the given {@link String} represents, or a value of {@code null} if no {@link Team} can be found with that id.
	 */
	public static ObjectHolder<Team> getTeam(String teamId) {
		for (Team team : DataManager.TeamData.loadedTeams) if (team.getTeamId().equals(teamId)) return new ObjectHolder<>(team);
		return new ObjectHolder<>();
	}

	public static ObjectHolder<Team> getTeamByName(String name) {
		for (Team team : DataManager.TeamData.loadedTeams) if (team.getName().equals(name)) return new ObjectHolder<>(team);
		return new ObjectHolder<>();
	}

	public static ObjectHolder<Team> getTeam(ChunkPos pos, ResourceLocation dimension) {
		for (Team team : DataManager.TeamData.loadedTeams) {
			if (team.hasChunkPos(dimension, pos)) return new ObjectHolder<>(team);
		}
		return new ObjectHolder<>();
	}

	public static boolean isRoleHigher(Team team, String role, String possiblyBiggerRole) {
		for (String currRole : team.getRoleRanking()) {
			if (Objects.equals(currRole, possiblyBiggerRole)) return true;
			else if (Objects.equals(currRole, role)) return false;
		}
		return false;
	}

	public static boolean teamExists(String teamName) {
		return getTeamByName(teamName).isPresent();
    }

	/**
	 * @return A random id for an {@link Team} to use.
	 */
    public static String createRandomTeamId() {
		LocalDateTime time = LocalDateTime.now();
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		sb
			.append("team_")
			.append(UUID.randomUUID().toString().substring(4))
			.append(String.valueOf(random.nextBoolean()), 1, 4)
			.append(time.getHour() / 13 + time.getNano())
			.append(Month.values()[random.nextInt(0, Month.values().length - 1)].toString(), 0, 2)
			.append(random.nextInt(1000, 9999))
			.append(UUID.randomUUID().toString().substring(7))
			.append(time.getDayOfYear());
		if (random.nextBoolean()) sb.append(UUID.randomUUID().toString(), 3, 5);
		else sb.append(time.getDayOfMonth());
		return sb.toString();
    }

	public static boolean endWar(War war, Player player) {
		if (!(TeamUtils.getTeam(player).get() instanceof Team team)) return false;
		if (!TeamUtils.canPlayerDo(team, player, "declareWar")) return false;
		deleteWar(war);
		return true;
	}

	public static void deleteWar(War war) {
		DataManager.WarData.loadedWars.remove(war);
		PacketHandler.sendToAllPlayers(new BiRemoveWar(war));
	}

	/**
	 * @return A new {@link Team} with the given parameters.
	 */
    public static Team createTeam(String name, Player player, Color color) {
        Team created = Team.TeamBuilder.create()
                .setName(name)
                .setTeamId(TeamUtils.createRandomTeamId())
                .addPlayer("owner", new ArrayList<>(List.of(player.getUUID())))
                .setColor(color)
                .build();

		DistHelper.runWhenOnServer(() -> () -> PacketHandler.sendToAllPlayers(new BiAddTeam(created)));

		return created;
    }

	/**
	 * @param teamId The receivingTeam to delete
	 */
	public static void removeTeam(String teamId) {
		DataManager.TeamData.loadedTeams.removeIf(team -> Objects.equals(team.getTeamId(), teamId));

		DistHelper.runWhenOnServer(() -> () -> PacketHandler.sendToAllPlayers(new S2CRemoveTeam(teamId)));
	}

	/**
	 * Claims the current chunk for the given player's receivingTeam.
	 * @return 1 if successful, -1 if failed (for /command usage)
	 */
	public static int claimCurrentChunk(Player player) {
		return getTeam(player).ifPresentOrElse(team -> claimChunk(team, getPlayerDimension(player), player.chunkPosition()), () -> -1);
	}

	/**
	 * Unclaims the current chunk for the given player's receivingTeam.
	 * @return 1 if successful, -1 if failed (for /command usage)
	 */
	public static int unclaimCurrentChunk(Player player) {
		return getTeam(player).ifPresentOrElse(team -> unclaimChunkAndUpdate(team, getPlayerDimension(player), player.chunkPosition()), () -> -1);
	}

	/**
	 * Check if a chunk has a CapitolBlock.
	 * @param pos The chunk position.
	 * @param dimension The dimension.
	 * @return If the chunk has a CapitolBlock.
	 */
	public static boolean chunkHasCapitolBlock(ChunkPos pos, ResourceLocation dimension) {
		AtomicBoolean result = new AtomicBoolean(false);
		for (Team team : DataManager.TeamData.loadedTeams) {
			team.getDimensionalData(dimension).getParentOfChunk(pos).ifPresent(
				capitolData -> result.set(capitolData.capitolBlockChunk == pos)
			);
		}
		return result.get();
	}

	/**
	 * Checks if nearby chunks in radius are claimed by player's receivingTeam.
	 * @param player Player to check
	 * @param radius The chunk radius around the player to check
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean chunkIsNearChildChunk(ChunkPos chunkPos, int radius, Player player) {
		ObjectHolder<Team> holder = getTeam(player);
		return holder.isPresent() && chunkIsNearChildChunk(chunkPos, radius, player.level().dimension().location(), holder.getOrThrow());
	}

	/**
	 * Checks if nearby chunks in radius are of a receivingTeam
	 * @param chunkPos origin on which to check around
	 * @param radius the radius to check around chunkPos
	 * @param dimension the dimension to check in
	 * @param team the receivingTeam to check
	 * @return wether any were found
	 */
	public static boolean chunkIsNearChildChunk(ChunkPos chunkPos, int radius, ResourceLocation dimension, Team team) {
		AtomicBoolean toReturn = new AtomicBoolean(false);
		chunkRadiusOperation(chunkPos, radius, inputChunk -> team.hasChunkPos(dimension, inputChunk), inputChunk -> toReturn.set(inputChunk != null));
		return toReturn.get();
	}

	public static void chunkRadiusOperation(ChunkPos chunkPos, int radius, Predicate<ChunkPos> filter, Consumer<@Nullable ChunkPos> processor) {
		radius++;
		for (int x = -1; x < radius; x++) {
			for (int z = -1; z < radius; z++) {
				ChunkPos currentChunkPos = new ChunkPos(chunkPos.x - x, chunkPos.z - z);
				if (filter.test(currentChunkPos)) {
					processor.accept(currentChunkPos);
				}
			}
		}
	}

	public static boolean canPlayerNotDoInChunk(BlockPos blockPos, Player player, String action) {
		return canPlayerNotDoInChunk(new ChunkPos(blockPos), player, action);
	}

	public static boolean canPlayerNotDoInChunk(ChunkPos chunkPos, Player player, String action) {
		ObjectHolder<Team> team = getTeam(chunkPos, getPlayerDimension(player));
		if (team.isPresent()) return !canPlayerDo(team.getOrThrow(), player, action);
		return false;
	}
	/**
	 * Returns whether player can do X action based on their permission
	 */
	public static boolean canPlayerDo(Team team, Player player, String action) {
		System.out.println(getPlayerPermission(team, player));
		return getPlayerPermission(team, player).get(action);
	}

	public static Map<String, Boolean> getPlayerPermission(Team team, Player player) {
		return team.getPermission(team.getRole(player.getUUID()));
	}

	/**
	 * Check if player's receivingTeam owns chunk at position.
	 * @param player the player on which the receivingTeam shall be checked.
	 * @param chunkPos the position of the chunk.
	 */
	public static boolean isChunkParent(Player player, ResourceLocation dimension, ChunkPos chunkPos) {
		ObjectHolder<Team> holder = TeamUtils.getTeam(chunkPos, dimension);
		return holder.isPresent() && !Objects.equals(holder.getOrThrow().getRole(player.getUUID()), "non-member");
	}

	/**
	 * Check if receivingTeam owns chunk at position.
	 * @param team the receivingTeam on which the receivingTeam shall be checked.
	 * @param chunkPos the position of the chunk.
	 */
	public static boolean isChunkParent(Team team, ResourceLocation dimension, ChunkPos chunkPos) {
		return TeamUtils.isChildChunk(dimension, chunkPos) && team.getDimensionalData(dimension).getAllChildChunks().contains(chunkPos);
	}

	/**
	 * Claims chunks in a radius of a center position to a receivingTeam
	 * @param team The receivingTeam to claim the chunks to
	 * @param dimension The dimension to claim the chunks in
	 * @param chunkPos The center of the radius to claim the chunks in
	 * @param radius The radius itself
	 */
	public static void claimChunkRadius(Team team, ResourceLocation dimension, ChunkPos chunkPos, int radius) {
		chunkRadiusOperation(chunkPos, radius, inputChunk -> !isChildChunk(dimension, inputChunk), inputChunk -> claimChunk(team, dimension, inputChunk));
	}

	/**
	 * Unclaims chunks in a radius of a center position from a receivingTeam
	 * @param team The receivingTeam to unclaim the chunks from
	 * @param dimension The dimension to unclaim the chunks in
	 * @param chunkPos The center of the radius to unclaim the chunks in
	 * @param radius The radius itself
	 */
	public static void unclaimChunkRadius(Team team, ResourceLocation dimension, ChunkPos chunkPos, int radius) {
		List<ChunkPos> toRemove = new ArrayList<>();
		chunkRadiusOperation(chunkPos, radius, inputChunk -> isChildChunk(dimension, inputChunk), toRemove::add);
		unclaimChunks(team, dimension, toRemove);
	}

	public static int unclaimChunks(Team team, ResourceLocation dimension, List<ChunkPos> chunkPosList) {
		if (CapitolConfig.SERVER.debugLogs.get()) Capitol.LOGGER.info("Unclaiming chunk " + chunkPosList + " in dimension " + dimension + " from receivingTeam '" + team.getName() + "'");

		team.getDimensionalData(dimension).removeChildChunks(chunkPosList);

		DistHelper.runWhenOnServer(() -> () -> PacketHandler.sendToAllPlayers(new BiRemoveChunk(dimension, chunkPosList)));

		return 1;
	}

	// Don't name atomic variables as optional, that makes no sense
	public static Optional<Team.CapitolData> getNearestParent(ResourceLocation dimension, ChunkPos chunkPos) {
		AtomicReference<Optional<Team.CapitolData>> reference = new AtomicReference<>(Optional.empty());
		TeamUtils.chunkRadiusOperation(chunkPos, 1, inputChunk -> reference.get().isEmpty(), inputChunk -> {
			ObjectHolder<Team> team = getTeam(inputChunk, dimension);
			if (team.isPresent()) reference.set(team.getOrThrow().getDimensionalData(dimension).getParentOfChunk(inputChunk));
		});
		return reference.get();
	}

	/**
	 * Claims the given chunk for the given receivingTeam,
	 * If the chunk has no near chunks nearby to take ownership,
	 * it will default to create a capitolblock claim, please check before to avoid this effect
	 * @return 1 if successful, -1 if failed (for /command usage)
	 */
	public static int claimChunk(Team team, ResourceLocation dimension, ChunkPos pos) {
		if (CapitolConfig.SERVER.debugLogs.get()) Capitol.LOGGER.info("Claiming chunk " + pos + " in dimension " + dimension + " for receivingTeam '" + team.getName() + "'");

		Team.CapitolData parent = getNearestParent(dimension, pos).orElseGet(() -> {
			Team.CapitolData def = new Team.CapitolData(pos);
			team.getDimensionalData(dimension).addCapitolData(def);
			return def;
		});

		parent.addChild(pos);

		DistHelper.runWhenOnServer(() -> () -> PacketHandler.sendToAllPlayers(new BiAddChunk(pos, team.getTeamId(), dimension)));

		return 1;
	}

	public static int unclaimCurrentChunkAndUpdate(Player player) {
		return unclaimChunkAndUpdate(getTeam(player).getOrThrow(), player.level().dimension().location(), player.chunkPosition());
	}

	public static int unclaimChunkAndUpdate(Team team, ResourceLocation dimension, ChunkPos chunkPos) {
		team.getDimensionalData(dimension).getParentOfChunk(chunkPos).ifPresent(
			capitolData -> {
				List<ChunkPos> childChunks = capitolData.getChildChunks();
				List<ChunkPos> connectedChunks = getConnectedChunks(capitolData, childChunks, new ArrayList<>()).stream().distinct().toList();

				unclaimChunks(team, dimension, childChunks.stream().filter(chunkPos1 -> !connectedChunks.contains(chunkPos1)).toList());
			}
		);
		return 1;
	}

	private static List<ChunkPos> getConnectedChunks(Team.CapitolData capitolData, List<ChunkPos> childChunks, List<ChunkPos> collected) {
		chunkRadiusOperation(capitolData.capitolBlockChunk, 1, chunkPos -> !collected.contains(chunkPos) && childChunks.contains(chunkPos),
			chunkPos -> {
			collected.add(chunkPos);
			collected.addAll(getConnectedChunks(capitolData, childChunks, collected));
		});
		return collected;
	}

	/**
	 * Unclaims the given chunk from the given receivingTeam.
	 * @param team      The receivingTeam to unclaim for.
	 * @param dimension The dimension of the chunk.
	 * @param chunkPos  The position of the chunk.
	 */
	// TODO: ChunkUnclaimedEvent?
	public static void unclaimChunk(Team team, ResourceLocation dimension, ChunkPos chunkPos) {
		if (CapitolConfig.SERVER.debugLogs.get()) Capitol.LOGGER.info("Unclaiming chunk " + chunkPos + " in dimension " + dimension + " from receivingTeam '" + team.getName() + "'");

		team.getDimensionalData(dimension).removeChildChunk(chunkPos);

		DistHelper.runWhenOnServer(() -> () -> PacketHandler.sendToAllPlayers(new BiRemoveChunk(dimension, List.of(chunkPos))));

	}

	public static void claimChunkIfNotClaimed(Team team, ResourceLocation dimension, ChunkPos pos) {
		if (!TeamUtils.isChildChunk(dimension, pos)) TeamUtils.claimChunk(team, dimension, pos);
	}

	public static void unclaimChunkIfFromTeam(Team team, ResourceLocation dimension, ChunkPos pos) {
		if (TeamUtils.isChildChunk(dimension, pos) && TeamUtils.getTeam(pos, dimension).getOrThrow().equals(team)) TeamUtils.unclaimChunk(team, dimension, pos);
	}

	public static List<Team> getTeamAndAllies(Team team) {
		List<Team> teams = new ArrayList<>();
		teams.add(team);
		team.getAllies().forEach(teamId -> getTeam(teamId).ifPresent(teams::add));
		return teams;
	}

	@SuppressWarnings("DataFlowIssue") // Only called server-side. We know the world is not null at this point, because that's impossible.
	public static boolean isChunkEdgeOfClaims(ChunkAccess chunk) {
		// If you're reading this, how can you still put var despite legit stating the type with (Level)?
		// Because I'm fucking lazy idk man - Matty
		// I was told never to keep assert on shipping
		// There's an assertion arg for jvm that makes them act like proper throwables but it's disabled by default, we know the world isn't null, assert is to just make intellij shut up about it
		ChunkPos pos = chunk.getPos();
		int radius = 1;
		radius++;
		for (int x = -1; x < radius; x++) for (int z = -1; z < radius; z++)
			if (!TeamUtils.isChildChunk((chunk.getLevel()).dimension().location(), new ChunkPos(pos.x - x, pos.z - z)))
				return true;
		return false;
	}
}