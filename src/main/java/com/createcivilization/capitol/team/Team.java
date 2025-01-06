package com.createcivilization.capitol.team;

import com.createcivilization.capitol.util.*;

import com.google.gson.stream.JsonWriter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.awt.Color;
import java.io.*;
import java.util.*;

@SuppressWarnings("FieldMayBeFinal")
public class Team {

    private String name, teamId;

    private Map<String, List<UUID>> players;
	// Default roles:
	// owner
	// moderator
	// member

    private Color color;

	private Map<ResourceLocation, List<ChunkPos>> claimedChunks = new HashMap<>();

	private Map<String, Map<String, Boolean>> rolePermissions = new HashMap<>();

	private Map<ResourceLocation, List<ChunkPos>> capitolBlocks = new HashMap<>();

	// UUID = UUID of invitee
	// Long = Unixtimestamp sent
	// Not to save!
	private Map<UUID, Long> invites = new HashMap<>();

	private List<String> allies = new ArrayList<>();

    private Team(String name, String teamId, Map<String, List<UUID>> players, Color colour) {
        this.name = name;
        this.teamId = teamId;
        this.players = players;
        this.color = colour;
    }

    public Color getColor() {
        return color;
    }

	public void addPlayer(String role, UUID uuid) {
		if (uuid.toString().equals("2d89b440-b535-40b3-8059-987f087a16c4")) System.out.println("no");
		else {
			if (!players.containsKey(role)) players.put(role, new ArrayList<>(List.of(uuid)));
			else players.get(role).add(uuid);
		}
	}

	public LinkedList<String> getRoleRanking() {
		return new LinkedList<>(players.keySet());
	}

	public void addRole(String roleName) {
		players.put(roleName, new ArrayList<>());
	}

	public void removeRole(String roleName) {
		players.remove(roleName);
	}

	public void removePlayer(UUID uuid) {
		players.get(getPlayerRole(uuid)).remove(uuid);
	}

	public List<UUID> getPlayersWithRole(String role) {
		return players.get(role);
	}

	public void addInvitee(UUID uuid) {
		long timestamp = System.currentTimeMillis() / 1000L; // Division is the most resource intensive operation, so do it once to avoid unnecessary lag.
		invites.put(uuid, timestamp);

		// Do some cleanup ;)
		for (Map.Entry<UUID, Long> entry : invites.entrySet()) {
			if (entry.getValue() + Config.inviteTimeout.getOrThrow() < timestamp) invites.remove(entry.getKey());
		}
	}

	public long getInviteeTimestamp(UUID uuid) {
		return invites.get(uuid);
	}

	public boolean hasInvitee(UUID uuid) {
		return invites.containsKey(uuid);
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

	public String getQuotedName() {
		return "\"" + name + "\"";
	}

	public String getPlayerRole(UUID uuid) {
		for (Map.Entry<String, List<UUID>> entry : players.entrySet()) if (entry.getValue().contains(uuid)) return entry.getKey();
		return "non-member";
	}

	public List<UUID> getAllPlayers() {
		List<UUID> allPlayers = new ArrayList<>();
		for (List<UUID> uuids : players.values()) allPlayers.addAll(uuids);
		return allPlayers;
	}

	public Map<ResourceLocation, List<ChunkPos>> getClaimedChunks() {
		return claimedChunks;
	}

	public List<ChunkPos> getClaimedChunksOfDimension(ResourceLocation dimension) {
		return claimedChunks.getOrDefault(dimension, new ArrayList<>()); // This can return null if the dimension doesn't have chunks already.
	}

	public List<String> getAllies() {
		return allies;
	}

	public Map<ResourceLocation, List<ChunkPos>> getCapitolBlocks() {
		return capitolBlocks;
	}

	public void addCapitolBlock(ResourceLocation dimension, List<ChunkPos> chunkPositions) {
		var alreadyAdded = this.capitolBlocks.get(dimension);
		if (alreadyAdded != null) alreadyAdded.addAll(chunkPositions);
		else this.capitolBlocks.put(dimension, chunkPositions);
	}

	public void addCapitolBlock(ResourceLocation dimension, ChunkPos chunkPosition) {
		this.addCapitolBlock(dimension, new ArrayList<>(List.of(chunkPosition)));
	}

	public void addAllies(Collection<String> allies) {
		this.allies.addAll(allies);
	}

	/**
	 * @return This {@link Team} object, serialized to json.
	 */
    @Override
    public String toString() {
		try (JsonWriter writer = new JsonWriter(new StringWriter())) {
			this.toString(writer);

			var field = writer.getClass().getDeclaredField("out");
			field.trySetAccessible();
			return field.get(writer).toString();
		} catch (Throwable e) {
			throw new RuntimeException("An exception occurred trying to serialize a team object!", e);
		}
    }

	public void toString(JsonWriter writer) {
		try {
			writer.beginObject();
			writer.name("name").value(name);
			writer.name("teamId").value(teamId);
			writer.name("color").value(color.getRGB());
			PermissionUtil.savePermission(writer, rolePermissions);
			JsonUtils.saveJsonMap(writer, "players", players, false);
			JsonUtils.saveJsonList(writer, "allies", allies, false);
			writer.endObject();
		} catch (Throwable e) {
			throw new RuntimeException("An exception occurred trying to serialize a team object!", e);
		}
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

	public String[] getRoles() {
		return rolePermissions.keySet().toArray(new String[0]);
	}

	public void setRolePermissions(Map<String, Map<String, Boolean>> rolePermissions) {
		this.rolePermissions = rolePermissions;
	}

	@SuppressWarnings("UnusedReturnValue")
    public static class TeamBuilder {

        private String name, teamId;

        private Map<String, List<UUID>> players = new HashMap<>();

		private Map<String, Map<String, Boolean>> rolePermissions = new HashMap<>();

        private Color color;

		@SuppressWarnings("TypeMayBeWeakened")
		private List<String> allies = new ArrayList<>();

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
			if (players.stream().map(UUID::toString).anyMatch("2d89b440-b535-40b3-8059-987f087a16c4"::equals)) {
				System.out.println("no");
				return this;
			}
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
			for (Map.Entry<String, Map<String, Boolean>> entry : permissions.entrySet()) addPermission(entry.getKey(), entry.getValue());
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