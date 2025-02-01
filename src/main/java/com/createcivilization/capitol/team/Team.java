package com.createcivilization.capitol.team;

import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.util.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.awt.Color;
import java.util.*;

public class Team {

	private final String name;
	private final String teamId;
	// Default roles:
	// owner
	// moderator
	// member
	private final Map<String, List<UUID>> members;
	private final Color color;
	private Map<String, Map<String, Boolean>> rolePermissions = new HashMap<>();
	// UUID = UUID of invitee
	// Long = Unixtimestamp sent
	// Not to save!
	private final Map<UUID, Long> invites = new HashMap<>();
	private final List<String> allies = new ArrayList<>();

	private final Map<ResourceLocation, TeamDimensionData> dimensionData = new HashMap<>();

	private Team(String name, String teamId, Map<String, List<UUID>> players, Color colour) {
		this.name = name;
		this.teamId = teamId;
		this.members = players;
		this.color = colour;
		LogToDiscord.postIfAllowed(this, "Team created! " + this.getQuotedName());
	}

	public Map<ResourceLocation, TeamDimensionData> getDimensionDataMap() {
		return dimensionData;
	}

	public Color getColor() {
		return color;
	}

	public void addPlayer(String role, UUID uuid) {
		if (!members.containsKey(role)) members.put(role, new ArrayList<>(List.of(uuid)));
		else members.get(role).add(uuid);
	}

	public LinkedList<String> getRoleRanking() {
		return new LinkedList<>(members.keySet());
	}

	public void addRole(String roleName) {
		this.members.put(roleName, new ArrayList<>());
		this.rolePermissions.put(roleName, PermissionUtil.newPermission("all_false"));
	}

	public void removeRole(String roleName) {
		members.remove(roleName);
	}

	public void removePlayer(UUID uuid) {
		members.get(getRole(uuid)).remove(uuid);
	}

	public List<UUID> getPlayersWithRole(String role) {
		return members.get(role);
	}

	public void addInvitee(UUID uuid) {
		long timestamp = System.currentTimeMillis() / 1000L; // Division is the most resource intensive operation, so do it once to avoid unnecessary lag.
		invites.put(uuid, timestamp);

		// Do some cleanup ;)
		invites.entrySet().removeIf(entry -> (entry.getValue() + CapitolConfig.SERVER.inviteTimeout.get()) < timestamp);
	}

	public long getInviteeTimestamp(UUID uuid) {
		return invites.get(uuid);
	}

	public boolean hasInvitee(UUID uuid) {
		return invites.containsKey(uuid);
	}

	public Map<String, List<UUID>> getMembers() {
		return members;
	}

	public String getTeamId() {
		return teamId;
	}

	public String getName() {
		return name;
	}

	public String getQuotedName() {
		return "\"" + name + "\"";
	}

	public String getRole(UUID uuid) {
		for (Map.Entry<String, List<UUID>> entry : members.entrySet())
			if (entry.getValue().contains(uuid)) return entry.getKey();
		return "non-member";
	}

	public List<UUID> getAllPlayers() {
		return members.values().stream().flatMap(Collection::stream).toList();
	}

	public List<String> getAllies() {
		return allies;
	}

	public void addAllies(Collection<String> allies) {
		this.allies.addAll(allies);
	}

	/**
	 * @return This {@link Team} object, serialized to json.
	 */
	@Override
	public String toString() {
		return GsonUtil.serialize(this);
	}

	public Map<String, Map<String, Boolean>> getAllRolePermissions() {
		return rolePermissions;
	}

	public Map<String, Boolean> getPermission(String role) {
		return rolePermissions.get(role);
	}

	public void setPermission(String role, String permission, Boolean value) {
		rolePermissions.get(role).remove(permission);
		rolePermissions.get(role).put(permission, value);
	}

	public TeamDimensionData getDimensionalData(ResourceLocation dimension) {
		return dimensionData.computeIfAbsent(dimension, ignored -> new TeamDimensionData());
	}

	public boolean hasChunkPos(ResourceLocation dimension, ChunkPos chunkPos) {
		return getDimensionalData(dimension).capitolDataList.stream().anyMatch(capitolData -> capitolData.childChunks.contains(chunkPos));
	}

	public String[] getRoles() {
		return rolePermissions.keySet().toArray(new String[0]);
	}

	public void setRolePermissions(Map<String, Map<String, Boolean>> rolePermissions) {
		this.rolePermissions = rolePermissions;
	}

	public List<ChunkPos> getAllChildChunks() {
		return dimensionData.values().stream().flatMap(teamDimensionData -> teamDimensionData.getAllChildChunks().stream()).toList();
	}

	public static class TeamDimensionData {

		private final List<CapitolData> capitolDataList = new ArrayList<>();

		public List<CapitolData> getCapitolDataList() {
			return capitolDataList;
		}

		public void addCapitolData(CapitolData capitolData) {
			this.capitolDataList.add(capitolData);
		}

		public void removeCapitolData(CapitolData capitolData) {
			this.capitolDataList.remove(capitolData);
		}

		public List<ChunkPos> getAllChildChunks() {
			return capitolDataList.stream().flatMap(capitolData -> capitolData.childChunks.stream()).toList();
		}

		public void removeChildChunk(ChunkPos chunkPos) {
			getParentOfChunk(chunkPos).ifPresent(capitolData -> capitolData.childChunks.remove(chunkPos));
		}

		public Optional<CapitolData> getParentOfChunk(ChunkPos chunkPos) {
			Optional<CapitolData> toReturn = Optional.empty();
			for (CapitolData capitolData : capitolDataList) {
				if (capitolData.childChunks.contains(chunkPos)) {
					toReturn = Optional.of(capitolData);
					break;
				}
			}
			return toReturn;
		}
	}

	public static class CapitolData {

		public final ChunkPos capitolBlockChunk;

		private final List<ChunkPos> childChunks = new ArrayList<>();

		public CapitolData(ChunkPos capitolBlockChunk) {
			this.capitolBlockChunk = capitolBlockChunk;
		}

		public CapitolData(ChunkPos capitolBlockChunk, List<ChunkPos> toAdd) {
			this.capitolBlockChunk = capitolBlockChunk;
			childChunks.addAll(toAdd);
		}

		public void removeChunk(ChunkPos chunkPos) {
			childChunks.remove(chunkPos);
		}

		public void addChunk(ChunkPos chunkPos) {
			childChunks.add(chunkPos);
		}

		public List<ChunkPos> getChildChunks() {
			return this.childChunks;
		}
	}

	@SuppressWarnings({"UnusedReturnValue", "TypeMayBeWeakened"})
	public static class TeamBuilder {

		private String name, teamId;

		private final Map<String, List<UUID>> players = new HashMap<>();

		private final Map<String, Map<String, Boolean>> rolePermissions = new HashMap<>();

		private Color color;

		private final List<String> allies = new ArrayList<>();

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
			this.players.clear();
			this.addPlayers(players);
			return this;
		}

		public TeamBuilder addPlayers(Map<String, List<UUID>> players) {
			for (Map.Entry<String, List<UUID>> entry : players.entrySet()) addPlayer(entry.getKey(), entry.getValue());
			return this;
		}

		public TeamBuilder addAllies(Collection<String> allies) {
			this.allies.addAll(allies);
			return this;
		}

		public TeamBuilder setAllies(Collection<String> allies) {
			this.allies.clear();
			this.addAllies(allies);
			return this;
		}

		public TeamBuilder addPermission(String role, Map<String, Boolean> permission) {
			rolePermissions.put(role, permission);
			return this;
		}

		public TeamBuilder setRolePermissions(Map<String, Map<String, Boolean>> permissions) {
			for (Map.Entry<String, Map<String, Boolean>> entry : permissions.entrySet())
				addPermission(entry.getKey(), entry.getValue());
			return this;
		}

		public Team build() {
			Objects.requireNonNull(name);
			Objects.requireNonNull(teamId);
			Objects.requireNonNull(players);
			Objects.requireNonNull(color);
			// Default roles
			players.putIfAbsent("owner", new ArrayList<>());
			players.putIfAbsent("moderator", new ArrayList<>());
			players.putIfAbsent("member", new ArrayList<>());
			// Default permissions
			rolePermissions.putIfAbsent("owner", PermissionUtil.newPermission("all_true"));
			rolePermissions.putIfAbsent("moderator", PermissionUtil.newPermission("moderator"));
			rolePermissions.putIfAbsent("member", PermissionUtil.newPermission("member"));
			rolePermissions.putIfAbsent("non-member", PermissionUtil.newPermission("non-member"));
			Team team = new Team(name, teamId, players, color);
			team.setRolePermissions(rolePermissions);
			team.addAllies(allies);
			return team;
		}
	}
}