package com.createcivilization.capitol.common.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * A war between two teams, migrated from the old War System.
 * <p>
 * Only {@link #declaringTeamId()}, {@link #receivingTeamId()} and
 * {@link #timeOfCreation()} are persisted. The rest of the fields are filled
 * in when a war is read back out of the database so the client can render the
 * war display without having the whole team dataset synced.
 */
public record War(
	UUID declaringTeamId,
	UUID receivingTeamId,
	long timeOfCreation,
	String declaringTeamName,
	String receivingTeamName,
	int declaringSidePlayers,
	int receivingSidePlayers,
	int receivingTeamChunks,
	int receivingTeamCapitols
) {

	public boolean isDeclarer(UUID teamId) {
		return declaringTeamId.equals(teamId);
	}

	public boolean isReceiver(UUID teamId) {
		return receivingTeamId.equals(teamId);
	}

	// is the team (or one of its allies) a direct participant?
	public boolean isParticipant(UUID teamId) {
		return isDeclarer(teamId) || isReceiver(teamId);
	}

	// names come from a JOIN on teams; stats are filled in separately (not persisted)
	public static War fromResultSet(ResultSet rs) throws SQLException {
		return new War(
			UUID.fromString(rs.getString("declaring_team_id")),
			UUID.fromString(rs.getString("receiving_team_id")),
			rs.getLong("time_of_creation"),
			rs.getString("declaring_team_name"),
			rs.getString("receiving_team_name"),
			0, 0, 0, 0
		);
	}

	public static StreamCodec<ByteBuf, War> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public War decode(ByteBuf buffer) {
			UUID declaringTeamId = UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(buffer));
			UUID receivingTeamId = UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(buffer));
			long timeOfCreation = ByteBufCodecs.VAR_LONG.decode(buffer);
			String declaringTeamName = ByteBufCodecs.STRING_UTF8.decode(buffer);
			String receivingTeamName = ByteBufCodecs.STRING_UTF8.decode(buffer);
			int declaringSidePlayers = ByteBufCodecs.INT.decode(buffer);
			int receivingSidePlayers = ByteBufCodecs.INT.decode(buffer);
			int receivingTeamChunks = ByteBufCodecs.INT.decode(buffer);
			int receivingTeamCapitols = ByteBufCodecs.INT.decode(buffer);
			return new War(
				declaringTeamId, receivingTeamId, timeOfCreation,
				declaringTeamName, receivingTeamName,
				declaringSidePlayers, receivingSidePlayers,
				receivingTeamChunks, receivingTeamCapitols
			);
		}

		@Override
		public void encode(ByteBuf buffer, War war) {
			ByteBufCodecs.STRING_UTF8.encode(buffer, war.declaringTeamId().toString());
			ByteBufCodecs.STRING_UTF8.encode(buffer, war.receivingTeamId().toString());
			ByteBufCodecs.VAR_LONG.encode(buffer, war.timeOfCreation());
			ByteBufCodecs.STRING_UTF8.encode(buffer, war.declaringTeamName());
			ByteBufCodecs.STRING_UTF8.encode(buffer, war.receivingTeamName());
			ByteBufCodecs.INT.encode(buffer, war.declaringSidePlayers());
			ByteBufCodecs.INT.encode(buffer, war.receivingSidePlayers());
			ByteBufCodecs.INT.encode(buffer, war.receivingTeamChunks());
			ByteBufCodecs.INT.encode(buffer, war.receivingTeamCapitols());
		}
	};
}