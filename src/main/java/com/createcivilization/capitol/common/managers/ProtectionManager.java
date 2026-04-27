package com.createcivilization.capitol.common.managers;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamProtection;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.joml.Vector3dc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Central protection resolution for Capitol's claim system.
 * Inspired by Open Parties and Claims (OPAC) by Xaero (LGPL-3.0).
 * See: <a href="https://github.com/thexaero/open-parties-and-claims">...</a>
 */
public class ProtectionManager {

	public enum Result { ALLOW, DENY, PASS }

	private static ResourceMatcher blockBreakExceptions;
	private static ResourceMatcher blockPlaceExceptions;
	private static ResourceMatcher blockInteractExceptions;
	private static ResourceMatcher itemUseExceptions;
	private static ResourceMatcher entitiesAllowedToGrief;
	private static ResourceMatcher protectedEntities;
	private static ResourceMatcher entityClaimBarrier;
	private static Set<String> teamConfigurableKeys;

	private ProtectionManager() {}

	/** Rebuilds all config-driven matchers. Call on server start and config reload. */
	public static void reload() {
		blockBreakExceptions = ResourceMatcher.fromConfigList(CapitolConfig.BLOCK_BREAK_EXCEPTIONS.get());
		blockPlaceExceptions = ResourceMatcher.fromConfigList(CapitolConfig.BLOCK_PLACE_EXCEPTIONS.get());
		blockInteractExceptions = ResourceMatcher.fromConfigList(CapitolConfig.BLOCK_INTERACTION_EXCEPTIONS.get());
		itemUseExceptions = ResourceMatcher.fromConfigList(CapitolConfig.ITEM_USE_EXCEPTIONS.get());
		entitiesAllowedToGrief = ResourceMatcher.fromConfigList(CapitolConfig.ENTITIES_ALLOWED_TO_GRIEF.get());
		protectedEntities = ResourceMatcher.fromConfigList(CapitolConfig.PROTECTED_ENTITIES.get());
		entityClaimBarrier = ResourceMatcher.fromConfigList(CapitolConfig.ENTITY_CLAIM_BARRIER.get());
		teamConfigurableKeys = new HashSet<>(CapitolConfig.TEAM_CONFIGURABLE_PROTECTIONS.get());

		Capitol.LOGGER.info("ProtectionManager loaded");
	}

	/** Returns whether the given protection can be toggled per-team via config. */
	public static boolean isTeamConfigurable(TeamProtection protection) {
		return teamConfigurableKeys != null && teamConfigurableKeys.contains(protection.getKey());
	}

	/**
	 * Core player-vs-block check for real-world chunks with a config exception list.
	 * Used for break, place, interact, container, and redstone checks.
	 */
	public static Result checkBlockAction(Player player, Block block, Permission permission, Level level, ChunkPos pos, @Nullable ResourceMatcher exceptions) {
		if (player.hasPermissions(4)) return Result.ALLOW;

		Team team = database().getChunkOwner(pos, level);
		if (team == null) return Result.PASS;

		if (player instanceof FakePlayer) return resolveFakePlayer(player, level, team);

		if (exceptions != null && exceptions.matchesBlock(block)) return Result.ALLOW;

		return resolvePermission(player, permission, team);
	}

	/**
	 * Core player-vs-block check for sub-level blocks with a config exception list.
	 * When the sub-level overlaps a differently-owned real-world chunk and
	 * {@code SUBLEVEL_CLAIM_OVERLAP} is enabled, the player must satisfy both teams' permissions.
	 */
	public static Result checkSublevelBlockAction(Player player, Block block, Permission permission, SubLevelAccess subLevelAccess, @Nullable ResourceMatcher exceptions){
		if(player.hasPermissions(4)) return Result.ALLOW;

		Team team = database().getSubLevelOwner(subLevelAccess.getUniqueId());
		if(team == null) return Result.PASS;
		if(exceptions != null && exceptions.matchesBlock(block)) return Result.ALLOW;

		Vector3dc subLevelPosition = subLevelAccess.logicalPose().position();
		BlockPos subLevelBlockPos = new BlockPos((int) subLevelPosition.x(), (int) subLevelPosition.y(), (int) subLevelPosition.z());
		ChunkPos subLevelChunkPos = new ChunkPos(subLevelBlockPos);
		Team subLevelChunkTeam = database().getChunkOwner(subLevelChunkPos, player.level());

		if(subLevelChunkTeam != team && subLevelChunkTeam != null && CapitolConfig.SUBLEVEL_CLAIM_OVERLAP.get()){
			Result result = resolvePermission(player, permission, subLevelChunkTeam);
			if(result == Result.DENY){
				return resolvePermission(player, permission, team);
			}
			return result;
		}

		return resolvePermission(player, permission, team);

	}

	/** Checks whether {@code player} can break {@code block} in the real-world chunk at {@code pos}. */
	public static Result checkBlockBreak(Player player, Block block, Level level, ChunkPos pos) {
		return checkBlockAction(player, block, Permission.BREAK_BLOCKS, level, pos, blockBreakExceptions);
	}

	/** Checks whether {@code player} can place {@code block} in the real-world chunk at {@code pos}. */
	public static Result checkBlockPlace(Player player, Block block, Level level, ChunkPos pos) {
		return checkBlockAction(player, block, Permission.PLACE_BLOCKS, level, pos, blockPlaceExceptions);
	}

	/** Checks whether {@code player} can place {@code block} inside {@code subLevel}. */
	public static Result checkBlockPlace(Player player, Block block, SubLevelAccess subLevel){
		return checkSublevelBlockAction(player, block, Permission.PLACE_BLOCKS, subLevel, blockPlaceExceptions);
	}

	/** Checks whether {@code player} can interact with {@code block} in the real-world chunk at {@code pos}. */
	public static Result checkBlockInteract(Player player, Block block, Level level, ChunkPos pos) {
		return checkBlockAction(player, block, Permission.INTERACT_BLOCKS, level, pos, blockInteractExceptions);
	}

	/** Checks whether {@code player} can interact with {@code block} inside {@code subLevel}. */
	public static Result checkBlockInteract(Player player, Block block, SubLevelAccess subLevel){
		return checkSublevelBlockAction(player, block, Permission.INTERACT_BLOCKS, subLevel, blockInteractExceptions);
	}

	/** Checks whether {@code player} can open a container at the real-world chunk at {@code pos}. */
	public static Result checkContainerOpen(Player player, Block block, Level level, ChunkPos pos) {
		return checkBlockAction(player, block, Permission.OPEN_CONTAINERS, level, pos, blockInteractExceptions);
	}

	/** Checks whether {@code player} can open a container inside {@code subLevel}. */
	public static Result checkContainerOpen(Player player, Block block, SubLevelAccess subLevel){
		return checkSublevelBlockAction(player, block, Permission.OPEN_CONTAINERS, subLevel, blockInteractExceptions);
	}

	/** Checks whether {@code player} can trigger redstone at the real-world chunk at {@code pos}. */
	public static Result checkRedstoneInteract(Player player, Block block, Level level, ChunkPos pos) {
		return checkBlockAction(player, block, Permission.INTERACT_REDSTONE, level, pos, blockInteractExceptions);
	}

	/** Checks whether {@code player} can trigger redstone inside {@code subLevel}. */
	public static Result checkRedstoneInteract(Player player, Block block, SubLevelAccess subLevel){
		return checkSublevelBlockAction(player, block, Permission.INTERACT_REDSTONE, subLevel, blockInteractExceptions);
	}

	/** Checks whether {@code player} can use {@code item} in the real-world chunk at {@code pos}. */
	public static Result checkItemUse(Player player, Item item, Level level, ChunkPos pos) {
		if (player.hasPermissions(4)) return Result.ALLOW;

		Team team = database().getChunkOwner(pos, level);
		if (team == null) return Result.PASS;

		if (player instanceof FakePlayer) return resolveFakePlayer(player, level, team);

		if (itemUseExceptions.matchesItem(item)) return Result.ALLOW;

		return resolvePermission(player, Permission.USE_ITEMS, team);
	}

	/**
	 * Checks whether {@code player} can interact with or kill {@code target} in the chunk at {@code pos}.
	 * Entities not on the protected entities list are always allowed.
	 */
	public static Result checkEntityAction(Player player, Entity target, Permission permission, Level level, ChunkPos pos) {
		if (player.hasPermissions(4)) return Result.ALLOW;

		Team team = database().getChunkOwner(pos, level);
		if (team == null) return Result.PASS;

		if (player instanceof FakePlayer) return resolveFakePlayer(player, level, team);

		if (!isEntityProtected(target)) return Result.ALLOW;

		return resolvePermission(player, permission, team);
	}

	/**
	 * Generic player permission check with no exception lists.
	 * Used for toss, pickup, xp, teleport, frost walk, etc.
	 */
	public static Result checkPlayerAction(Player player, Permission permission, Level level, ChunkPos pos) {
		if (player.hasPermissions(4)) return Result.ALLOW;

		Team team = database().getChunkOwner(pos, level);
		if (team == null) return Result.PASS;

		if (player instanceof FakePlayer) return resolveFakePlayer(player, level, team);

		return resolvePermission(player, permission, team);
	}

	/**
	 * Checks whether an explosion at {@code pos} should be blocked.
	 * Entities in the {@code ENTITIES_ALLOWED_TO_GRIEF} list bypass the protection.
	 */
	public static Result checkExplosion(Level level, ChunkPos pos, @Nullable Entity source) {
		Team team = database().getChunkOwner(pos, level);
		if (team == null) return Result.PASS;

		if (source != null && entitiesAllowedToGrief.matchesEntity(source.getType())) return Result.ALLOW;

		return database().isProtectionEnabled(team, TeamProtection.EXPLOSION) ? Result.DENY : Result.ALLOW;
	}

	/** Checks whether fire is allowed to spread into the chunk at {@code targetPos}. */
	public static Result checkFireSpread(Level level, ChunkPos targetPos) {
		if (!CapitolConfig.PROTECT_FIRE_SPREAD.get()) return Result.PASS;

		Team team = database().getChunkOwner(targetPos, level);
		if (team == null) return Result.PASS;

		return database().isProtectionEnabled(team, TeamProtection.FIRE) ? Result.DENY : Result.ALLOW;
	}

	/** Checks whether a piston at {@code pistonPos} can push into the chunk at {@code targetPos}. */
	public static Result checkPistonCrossBoundary(Level level, ChunkPos pistonPos, ChunkPos targetPos) {
		if (!CapitolConfig.PROTECT_PISTONS.get()) return Result.PASS;
		return checkCrossBoundary(level, pistonPos, targetPos);
	}

	/**
	 * Checks whether fluid can flow from {@code sourcePos} into {@code targetPos}.
	 * Denied only when the target is claimed by a different team than the source (or the source is unclaimed).
	 */
	public static Result checkFluidFlow(Level level, ChunkPos sourcePos, ChunkPos targetPos) {
		if (!CapitolConfig.PROTECT_FLUID_FLOW.get()) return Result.PASS;

		Team sourceTeam = database().getChunkOwner(sourcePos, level);
		Team targetTeam = database().getChunkOwner(targetPos, level);

		if (sourceTeam == null && targetTeam == null) return Result.PASS;
		if (sameTeam(sourceTeam, targetTeam)) return Result.PASS;

		return targetTeam != null ? Result.DENY : Result.PASS;
	}

	/**
	 * Checks whether {@code entity} is allowed to grief a block in the chunk at {@code pos}.
	 * Entities in the {@code ENTITIES_ALLOWED_TO_GRIEF} list always bypass the protection.
	 */
	public static Result checkMobGriefing(Entity entity, Level level, ChunkPos pos) {
		Team team = database().getChunkOwner(pos, level);
		if (team == null) return Result.PASS;

		if (entitiesAllowedToGrief.matchesEntity(entity.getType())) return Result.ALLOW;

		return database().isProtectionEnabled(team, TeamProtection.MOB_GRIEFING) ? Result.DENY : Result.ALLOW;
	}

	/**
	 * Checks whether a non-player entity is allowed to enter the claimed chunk at {@code pos}.
	 * Only entities matching the {@code ENTITY_CLAIM_BARRIER} config list are blocked.
	 */
	public static Result checkEntityEnterClaim(Entity entity, Level level, ChunkPos pos) {
		if (entity instanceof Player || entityClaimBarrier.isEmpty()) return Result.PASS;

		Team team = database().getChunkOwner(pos, level);
		if (team == null) return Result.PASS;

		return entityClaimBarrier.matchesEntity(entity.getType()) ? Result.DENY : Result.PASS;
	}

	/** Checks whether a player walking over a crop in the chunk at {@code pos} should be prevented from trampling it. */
	public static Result checkCropTrample(Level level, ChunkPos pos) {
		if (!CapitolConfig.PROTECT_CROP_TRAMPLING.get()) return Result.PASS;

		Team team = database().getChunkOwner(pos, level);
		if (team == null) return Result.PASS;

		return database().isProtectionEnabled(team, TeamProtection.CROP_TRAMPLING) ? Result.DENY : Result.ALLOW;
	}

	/**
	 * Checks whether an actor inside a sub-level can act on a block inside another sub-level.
	 * Returns PASS if the target sub-level is unclaimed.
	 */
	public static Result checkSubLevelToSublevelActorAction(Level level, SubLevelAccess actorSubLevel, SubLevelAccess targetSubLevel) {
		Team targetOwner = database().getSubLevelOwner(targetSubLevel.getUniqueId());
		if (targetOwner == null) return Result.PASS;

		Team actorTeam = database().getSubLevelOwner(actorSubLevel.getUniqueId());
		return sameTeam(actorTeam, targetOwner) ? Result.ALLOW : Result.DENY;
	}

	/**
	 * Checks whether a contraption in the real world can act on a block inside a sub-level.
	 * Returns PASS if the target sub-level is unclaimed.
	 */
	public static Result checkWorldToSubLevelActorAction(Level level, ChunkPos actorChunk, SubLevelAccess targetSubLevel) {
		Team targetOwner = database().getSubLevelOwner(targetSubLevel.getUniqueId());
		if (targetOwner == null) return Result.PASS;

		Team actorTeam = database().getChunkOwner(actorChunk, level);
		return sameTeam(actorTeam, targetOwner) ? Result.ALLOW : Result.DENY;
	}

	/**
	 * Checks whether a actor inside a sub-level can act on a real-world chunk.
	 * Returns PASS if the target chunk is unclaimed.
	 */
	public static Result checkSubLevelToWorldActorAction(Level level, SubLevelAccess actorSubLevel, ChunkPos targetChunk) {
		Team targetTeam = database().getChunkOwner(targetChunk, level);
		if (targetTeam == null) return Result.PASS;

		Team actorTeam = database().getSubLevelOwner(actorSubLevel.getUniqueId());
		return sameTeam(actorTeam, targetTeam) ? Result.ALLOW : Result.DENY;
	}

	/** Checks whether a contraption actor (drill, harvester, plough) in a real-world chunk can act on {@code targetChunk}. */
	public static Result checkContraptionAction(Level level, ChunkPos actorChunk, ChunkPos targetChunk) {
		Team targetTeam = database().getChunkOwner(targetChunk, level);
		if (targetTeam == null) return Result.PASS;

		Team actorTeam = database().getChunkOwner(actorChunk, level);
		return sameTeam(actorTeam, targetTeam) ? Result.ALLOW : Result.DENY;
	}

	/** Checks whether a block at {@code blockChunk} can be assembled into a contraption anchored at {@code anchorChunk}. */
	public static Result checkContraptionAssemble(Level level, ChunkPos anchorChunk, ChunkPos blockChunk) {
		Team anchorTeam = database().getChunkOwner(anchorChunk, level);
		Team blockTeam = database().getChunkOwner(blockChunk, level);

		if (anchorTeam == null && blockTeam == null) return Result.PASS;
		if (sameTeam(anchorTeam, blockTeam)) return Result.ALLOW;

		return Result.DENY;
	}

	private static CapitolDatabase database() {
		return DatabaseManager.database;
	}

	/** Resolves a fake player's effective team from its current chunk position, then checks against {@code targetTeam}. */
	private static Result resolveFakePlayer(Player player, Level level, Team targetTeam) {
		Team sourceTeam = database().getChunkOwner(new ChunkPos(player.blockPosition()), level);
		return sameTeam(sourceTeam, targetTeam) ? Result.ALLOW : Result.DENY;
	}

	/**
	 * Resolves the player's permission against {@code team}.
	 * Individual overrides take priority over role-based permissions.
	 */
	private static Result resolvePermission(Player player, Permission permission, Team team) {
		// TODO: ally team permission check goes here

		long perms = database().getPlayerPermission(player, team);
		Long individual = database().getIndividualPermissions(player, team);
		if (individual != null) perms = perms | individual;
		return permission.hasPermission(perms) ? Result.ALLOW : Result.DENY;
	}

	/**
	 * Returns whether {@code entity} should be checked against claim permissions.
	 * Respects the ONLY/EXCEPT list type from config — in ONLY mode only listed entities are protected;
	 * in EXCEPT mode all entities except listed ones are protected.
	 */
	private static boolean isEntityProtected(Entity entity) {
		boolean onlyMode = CapitolConfig.ENTITY_PROTECTION_LIST_TYPE.get() == CapitolConfig.ListType.ONLY;
		if (protectedEntities.isEmpty()) return !onlyMode;
		boolean matches = protectedEntities.matchesEntity(entity.getType());
		return onlyMode == matches;
	}

	/** Denies cross-chunk actions when the two chunks are claimed by different teams. Shared by piston checks. */
	private static Result checkCrossBoundary(Level level, ChunkPos sourcePos, ChunkPos targetPos) {
		Team sourceTeam = database().getChunkOwner(sourcePos, level);
		Team targetTeam = database().getChunkOwner(targetPos, level);

		if (sourceTeam == null && targetTeam == null) return Result.PASS;
		if (sameTeam(sourceTeam, targetTeam)) return Result.PASS;

		return Result.DENY;
	}

	/** Returns true only when both teams are non-null and share the same ID. */
	private static boolean sameTeam(@Nullable Team a, @Nullable Team b) {
		return a != null && b != null && a.getId().equals(b.getId());
	}

	/**
	 * Matches resource locations against exact IDs, {@code #tags}, and {@code *wildcard} patterns
	 * parsed from config string lists.
	 */
	static class ResourceMatcher {
		private final List<Predicate<ResourceLocation>> matchers;
		private final List<String> tagStrings;

		private ResourceMatcher(List<Predicate<ResourceLocation>> matchers, List<String> tagStrings) {
			this.matchers = matchers;
			this.tagStrings = tagStrings;
		}

		/** Parses a config string list into exact, wildcard, and tag matchers. */
		static ResourceMatcher fromConfigList(List<? extends String> entries) {
			List<Predicate<ResourceLocation>> matchers = new ArrayList<>();
			List<String> tags = new ArrayList<>();

			for (String raw : entries) {
				String entry = raw.trim();
				if (entry.isEmpty()) continue;

				if (entry.startsWith("#")) {
					tags.add(entry.substring(1));
				} else if (entry.contains("*") || entry.contains("(")) {
					Pattern pattern = Pattern.compile(toRegex(entry));
					matchers.add(rl -> pattern.matcher(rl.toString()).matches());
				} else {
					ResourceLocation exact = ResourceLocation.tryParse(entry);
					if (exact != null) {
						matchers.add(exact::equals);
					} else {
						Capitol.LOGGER.warn("Invalid resource location in config: {}", entry);
					}
				}
			}

			return new ResourceMatcher(matchers, tags);
		}

		/** Returns true if {@code block}'s registry ID or tags match any entry in this matcher. */
		boolean matchesBlock(Block block) {
			ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
			if (matchesDirect(id)) return true;
			return matchesTags(BuiltInRegistries.BLOCK, id);
		}

		/** Returns true if {@code entityType}'s registry ID or tags match any entry in this matcher. */
		boolean matchesEntity(EntityType<?> entityType) {
			ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
			if (matchesDirect(id)) return true;
			return matchesTags(BuiltInRegistries.ENTITY_TYPE, id);
		}

		/** Returns true if {@code item}'s registry ID or tags match any entry in this matcher. */
		boolean matchesItem(Item item) {
			ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
			if (matchesDirect(id)) return true;
			return matchesTags(BuiltInRegistries.ITEM, id);
		}

		/** Returns true if this matcher has no entries (neither direct nor tag matchers). */
		boolean isEmpty() {
			return matchers.isEmpty() && tagStrings.isEmpty();
		}

		private boolean matchesDirect(ResourceLocation id) {
			for (var matcher : matchers) {
				if (matcher.test(id)) return true;
			}
			return false;
		}

		private <T> boolean matchesTags(net.minecraft.core.Registry<T> registry, ResourceLocation id) {
			var holder = registry.getHolder(id).orElse(null);
			if (holder == null) return false;

			for (String tagStr : tagStrings) {
				ResourceLocation tagId = ResourceLocation.tryParse(tagStr);
				if (tagId == null) continue;
				TagKey<T> tag = TagKey.create(registry.key(), tagId);
				if (holder.is(tag)) return true;
			}
			return false;
		}

		private static String toRegex(String entry) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < entry.length(); i++) {
				char c = entry.charAt(i);
				switch (c) {
					case '*' -> sb.append("[a-z0-9_/.-]*");
					case '(' -> sb.append("(?:");
					case ')' -> sb.append(')');
					case '|' -> sb.append('|');
					case '.' -> sb.append("\\.");
					default -> sb.append(c);
				}
			}
			return sb.toString();
		}
	}
}