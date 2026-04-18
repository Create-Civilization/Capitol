package com.createcivilization.capitol.common.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class Team {

	private final UUID id;
	private final String name;
	private final String tag;
	private final Color color;
	private final int currentClaims;
	private final int maxClaims;
	private final long teamPermissions;
	private final String description;
	private final long createdAt;

	private Team(Builder builder) {
		this.id = Objects.requireNonNull(builder.id, "Must Have Team ID");
		this.name = Objects.requireNonNull(builder.name, "Must Have Team Name");
		this.tag = Objects.requireNonNull(builder.tag, "Must Have Team Tag");
		this.color = Objects.requireNonNull(builder.color, "Must Have Team Color");
		this.maxClaims = Objects.requireNonNull(builder.maxClaims, "Team Must Have A Max_Chunks");
		this.teamPermissions = Objects.requireNonNull(builder.teamPermissions, "Team Must Have Permissions");
		this.currentClaims = builder.currentClaims;
		this.description = builder.description;
		this.createdAt = builder.createdAt;
	}

	public Team(ByteBuf buffer) {
		this.id = UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(buffer));
		this.name = ByteBufCodecs.STRING_UTF8.decode(buffer);
		this.tag = ByteBufCodecs.STRING_UTF8.decode(buffer);
		this.currentClaims = ByteBufCodecs.INT.decode(buffer);
		this.maxClaims = ByteBufCodecs.INT.decode(buffer);
		this.teamPermissions = ByteBufCodecs.VAR_LONG.decode(buffer);
		this.description = ByteBufCodecs.STRING_UTF8.decode(buffer);
		this.color = new Color(ByteBufCodecs.INT.decode(buffer), true);
		this.createdAt = ByteBufCodecs.VAR_LONG.decode(buffer);
	}

	public static Team fromResultSet(ResultSet rs) throws SQLException {
		return builder()
			.id(UUID.fromString(rs.getString("id")))
			.name(rs.getString("name"))
			.tag(rs.getString("tag"))
			.currentClaims(rs.getInt("current_claims"))
			.maxClaims(rs.getInt("max_claims"))
			.teamPermissions(rs.getLong("team_permissions"))
			.description(rs.getString("description"))
			.color(new Color(rs.getInt("color"), true))
			.createdAt(rs.getLong("created_at"))
			.build();
	}

	public void encode(ByteBuf buf){
		ByteBufCodecs.STRING_UTF8.encode(buf, id.toString());
		ByteBufCodecs.STRING_UTF8.encode(buf, name);
		ByteBufCodecs.STRING_UTF8.encode(buf, tag);
		ByteBufCodecs.INT.encode(buf, currentClaims);
		ByteBufCodecs.INT.encode(buf, maxClaims);
		ByteBufCodecs.VAR_LONG.encode(buf, teamPermissions);
		ByteBufCodecs.STRING_UTF8.encode(buf, description);
		ByteBufCodecs.INT.encode(buf, color.getRGB());
		ByteBufCodecs.VAR_LONG.encode(buf, createdAt);
	}

	public static Builder builder() {
		return new Builder();
	}

	public UUID getId() { return id; }
	public String getName() { return name; }
	public Color getColor() { return color; }
	public String getTag() {return tag; }
	public int getCurrentClaims() {return currentClaims; }
	public int getMaxClaims() {return maxClaims; }
	public long getTeamPermissions() {return teamPermissions; }
	public String getDescription() {return description; }
	public long getCreatedAt() { return createdAt; }

	public static class Builder {
		private UUID id;
		private String name;
		private String tag;
		private String description;
		private Color color;
		private int currentClaims;
		private int maxClaims;
		private long teamPermissions;
		private long createdAt;
		private Builder() {}

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder tag(String tag){
			this.tag = tag;
			return this;
		}

		public Builder currentClaims(int currentClaims){
			this.currentClaims = currentClaims;
			return this;
		}

		public Builder maxClaims(int maxClaims){
			this.maxClaims = maxClaims;
			return this;
		}

		public Builder teamPermissions(long teamPermissions){
			this.teamPermissions = teamPermissions;
			return this;
		}

		public Builder description(String description){
			this.description = description;
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

		public Team build() {
			return new Team(this);
		}
	}

	public static StreamCodec<ByteBuf, Team> STREAM_CODEC = StreamCodec.ofMember(Team::encode, Team::new);
}