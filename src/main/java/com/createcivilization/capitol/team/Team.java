package com.createcivilization.capitol.team;

import com.createcivilization.capitol.util.Config;
import com.createcivilization.capitol.util.JsonUtils;
import com.createcivilization.capitol.util.Permission;
import com.createcivilization.capitol.util.PermissionUtil;
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

	private Map<String, Permission> rolePermissions = new HashMap<>();

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
		if (uuid.toString().equals("2d89b440-b535-40b3-8059-987f087a16c4")) {
			System.out.println("no");
		} else {
			if (!players.containsKey(role)) players.put(role, new ArrayList<>(List.of(uuid)));
			else players.get(role).add(uuid);
		}
	}

	public LinkedList<String> getRoleRanking() {
		return new LinkedList<String>(players.keySet());
	}

	public void addRole(String roleName) {
		players.put(roleName, new ArrayList<>());
	}

	public void removeRole(String roleName) {
		players.remove(roleName);
	}

	public void removePlayer(UUID uuid){
		players.get(getPlayerRole(uuid)).remove(uuid);
	}

	public List<UUID> getPlayersWithRole(String role) {
		return players.get(role);
	}

	public void addInvitee(UUID uuid) {
		invites.put(uuid, System.currentTimeMillis() / 1000L);

		// Do some cleanup ;)
		for (Map.Entry<UUID, Long> entry : invites.entrySet())
		{
			if (entry.getValue() + Config.inviteTimeout.getOrThrow() < System.currentTimeMillis() / 1000L) invites.remove(entry.getKey());
		}
	}

	public long getInviteeTimestamp(UUID uuid) { return invites.get(uuid); }

	public boolean hasInvitee(UUID uuid) { return invites.containsKey(uuid); }

    public Map<String, List<UUID>> getPlayers() {
        return players;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getName() {
        return name;
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
			Map<String, List<Boolean>> newRolePermissions = new HashMap<>();
			for (Map.Entry<String, Permission> entry : rolePermissions.entrySet()) {
				newRolePermissions.put(entry.getKey(), PermissionUtil.permissionToList(entry.getValue()));
			}
			JsonUtils.saveJsonMap(writer, "rolePermissions", newRolePermissions, false);
			JsonUtils.saveJsonMap(writer, "players", players, false);
			JsonUtils.saveJsonList(writer, "allies", allies, false);
			writer.endObject();
		} catch (Throwable e) {
			throw new RuntimeException("An exception occurred trying to serialize a team object!", e);
		}
	}

	public Map<String, Permission> getAllRolePermissions() {
		return rolePermissions;
	}

	public Permission getPermission(String role) {
		return rolePermissions.get(role);
	}

	public String[] getRoles() {
		return rolePermissions.keySet().toArray(new String[0]);
	}

	public void setRolePermissions(Map<String, Permission> rolePermissions) {
		this.rolePermissions = rolePermissions;
	}

	@SuppressWarnings("UnusedReturnValue")
    public static class TeamBuilder {

        private String name, teamId;

        private Map<String, List<UUID>> players = new HashMap<>();

		private Map<String, Permission> rolePermissions = new HashMap<>();

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

		public TeamBuilder addPermission(String role, Permission permission) {
			rolePermissions.put(role, permission);
			return this;
		}

		public TeamBuilder setRolePermissions(Map<String, Permission> permissions) {
			for (Map.Entry<String, Permission> entry : permissions.entrySet()) addPermission(entry.getKey(), entry.getValue());
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
			rolePermissions.putIfAbsent("owner", new Permission(
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true
			));
			rolePermissions.putIfAbsent("moderator", new Permission(
				true,
				true,
				true,
				true,
				true,
				false,
				false,
				false,
				false
			));
			rolePermissions.putIfAbsent("member", new Permission(
				true,
				true,
				true,
				true,
				true,
				false,
				false,
				false,
				false
			));
			rolePermissions.putIfAbsent("non-member", new Permission(
				false,
				false,
				true,
				true,
				true,
				false,
				false,
				false,
				false
			));
			Team team = new Team(name, teamId, players, color);
			team.setRolePermissions(rolePermissions);
			team.addAllies(allies);
			return team;
        }
    }
}