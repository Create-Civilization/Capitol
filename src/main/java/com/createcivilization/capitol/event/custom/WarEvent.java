package com.createcivilization.capitol.event.custom;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.team.War;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.eventbus.api.Event;

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

	public static class ChunkTakenOverEvent extends WarEvent {

		private final ChunkAccess chunk;
		private final Team teamThatLostChunk;

		@Internal
		public ChunkTakenOverEvent(War war, ChunkAccess chunk, Team teamThatLostChunk) {
			super(war);
			this.chunk = chunk;
			this.teamThatLostChunk = teamThatLostChunk;
		}

		public ChunkAccess getChunk() {
			return this.chunk;
		}

		public Team getTeamThatLostChunk() {
			return this.teamThatLostChunk;
		}
	}
}