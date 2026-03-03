package com.createcivilization.capitol.old.old.event.custom;

import com.createcivilization.capitol.old.old.team.*;

import net.minecraft.world.level.chunk.ChunkAccess;

import net.neoforged.bus.api.Event;

import org.jetbrains.annotations.ApiStatus.Internal;

import java.time.LocalDateTime;

public abstract class WarEvent extends Event {

	private final War war;
	private final LocalDateTime timeStamp;

	@Internal
	public WarEvent(War war) {
		this.war = war;
		this.timeStamp = LocalDateTime.now();
	}

	public War getWar() {
		return this.war;
	}

	public LocalDateTime getTimeStamp() {
		return this.timeStamp;
	}

	public static class WarCreatedEvent extends WarEvent {

		@Internal
		public WarCreatedEvent(War war) {
			super(war);
		}
	}

	public static class ChunkTakenOverEvent extends Event {

		private final ChunkAccess chunk;
		private final OldTeam oldTeamThatLostChunk;

		@Internal
		public ChunkTakenOverEvent(ChunkAccess chunk, OldTeam oldTeamThatLostChunk) {
			this.chunk = chunk;
			this.oldTeamThatLostChunk = oldTeamThatLostChunk;
		}

		public ChunkAccess getChunk() {
			return this.chunk;
		}

		public OldTeam getTeamThatLostChunk() {
			return this.oldTeamThatLostChunk;
		}
	}
}