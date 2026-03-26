package com.createcivilization.capitol.common.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class Team {

	private final UUID id;
	private final String name;
	private final Color color;
	private final long createdAt;
	private final List<WeakReference<ChunkPos>> chunks;
	private final List<TeamMember> members;

	private Team(Builder builder) {
		this.id = Objects.requireNonNull(builder.id, "Must Have Team ID");
		this.name = Objects.requireNonNull(builder.name, "Must Have Team Name");
		this.color = Objects.requireNonNull(builder.color, "Must Have Team Color");
		this.createdAt = builder.createdAt;
		this.chunks = new ArrayList<>(builder.chunks);
		this.members = new ArrayList<>(builder.members);
	}

	public static Team fromResultSet(ResultSet rs) throws SQLException {
		return builder()
			.id(UUID.fromString(rs.getString("id")))
			.name(rs.getString("name"))
			.color(new Color(rs.getInt("color"), true))
			.createdAt(rs.getLong("created_at"))
			.build();
	}


	public static Builder builder() {
		return new Builder();
	}

	public boolean hasChunkAt(ChunkPos chunkPos) {
		return chunks.stream()
			.map(WeakReference::get)
			.anyMatch(chunkPos::equals);
	}

	public boolean hasPlayer(Player player) {
		UUID playerUuid = player.getUUID();
		return members.stream()
			.anyMatch(m -> m.playerUuid().equals(playerUuid));
	}

	public void addMember(TeamMember member) {
		members.add(member);
	}

	public void removeMember(UUID playerUuid) {
		members.removeIf(m -> m.playerUuid().equals(playerUuid));
	}

	public void addChunk(ChunkPos pos) {
		chunks.add(new WeakReference<>(pos));
	}

	public void removeChunk(ChunkPos pos) {
		chunks.removeIf(ref -> pos.equals(ref.get()));
	}

	public UUID getId() { return id; }
	public String getName() { return name; }
	public Color getColor() { return color; }
	public long getCreatedAt() { return createdAt; }
	public List<TeamMember> getMembers() { return Collections.unmodifiableList(members); }

	public static class Builder {
		private UUID id;
		private String name;
		private Color color;
		private long createdAt;
		private final List<WeakReference<ChunkPos>> chunks = new ArrayList<>();
		private final List<TeamMember> members = new ArrayList<>();

		private Builder() {}

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder color(Color color) {
			this.color = color;
			return this;
		}

		public Builder createdAt(long createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder addMember(TeamMember member) {
			this.members.add(member);
			return this;
		}

		public Builder addChunk(ChunkPos pos) {
			this.chunks.add(new WeakReference<>(pos));
			return this;
		}

		public Team build() {
			return new Team(this);
		}
	}
}