package com.createcivilization.capitol.common.events;

import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.War;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.Event;

import java.time.LocalDateTime;

// events posted by the war system so other systems can react to it
public abstract class WarEvent extends Event {

	private final War war;
	private final LocalDateTime timeStamp;

	protected WarEvent(War war) {
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

		public WarCreatedEvent(War war) {
			super(war);
		}
	}

	public static class ChunkTakenOverEvent extends Event {

		private final ChunkPos chunkPos;
		private final ResourceLocation dimension;
		private final Team teamThatLostChunk;

		public ChunkTakenOverEvent(ChunkPos chunkPos, ResourceLocation dimension, Team teamThatLostChunk) {
			this.chunkPos = chunkPos;
			this.dimension = dimension;
			this.teamThatLostChunk = teamThatLostChunk;
		}

		public ChunkPos getChunkPos() {
			return this.chunkPos;
		}

		public ResourceLocation getDimension() {
			return this.dimension;
		}

		public Team getTeamThatLostChunk() {
			return this.teamThatLostChunk;
		}
	}
}