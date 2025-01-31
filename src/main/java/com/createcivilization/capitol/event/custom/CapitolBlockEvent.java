package com.createcivilization.capitol.event.custom;

import com.createcivilization.capitol.block.custom.CapitolBlock;
import com.createcivilization.capitol.team.Team;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.minecraftforge.eventbus.api.*;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

public abstract class CapitolBlockEvent extends Event {

	private final CapitolBlock block;

	@Internal
	public CapitolBlockEvent(CapitolBlock block) {
		this.block = block;
	}

	public CapitolBlock getBlock() {
		return this.block;
	}

	public abstract static class CapitolBlockDestroyedOrPlaced extends CapitolBlockEvent {

		private final Team team;
		private final Player player;
		private final Stage stage;
		private final BlockPos pos;
		private final Level level;
		private final ResourceLocation dimension;
		public boolean isPlacedEvent;

		@Internal
		public CapitolBlockDestroyedOrPlaced(
			CapitolBlock block,
			Team team,
			Player player,
			BlockPos pos,
			Level level,
			Stage stage,
			boolean isPlacedEvent
		) {
			super(block);
			this.team = team;
			this.player = player;
			this.stage = stage;
			this.pos = pos;
			this.level = level;
			this.dimension = this.getLevel().dimension().location();
			this.isPlacedEvent = isPlacedEvent;
		}

		public Team getTeam() {
			return this.team;
		}

		public Player getPlayer() {
			return this.player;
		}

		public BlockPos getPos() {
			return this.pos;
		}

		public Level getLevel() {
			return this.level;
		}

		public ResourceLocation getDimension() {
			return this.dimension;
		}

		public Stage getStage() {
			return this.stage;
		}

		public boolean isPlacedEvent() {
			return this.isPlacedEvent;
		}

		public boolean isDestroyedEvent() {
			return !this.isPlacedEvent;
		}

		public enum Stage {
			PRE,
			POST,
			FAILED
		}

		public static class CapitolBlockPlacedEvent extends CapitolBlockDestroyedOrPlaced {

			@Internal
			public CapitolBlockPlacedEvent(
				CapitolBlock block,
				Team team,
				Player player,
				BlockPos pos,
				Level level,
				Stage stage
			) {
				super(
					block,
					team,
					player,
					pos,
					level,
					stage,
					true
				);
			}

			public static class PreClaimEvent extends CapitolBlockPlacedEvent {

				@Internal
				public PreClaimEvent(
					CapitolBlock block,
					Team team,
					Player player,
					BlockPos pos,
					Level level
				) {
					super(
						block,
						team,
						player,
						pos,
						level,
						Stage.PRE
					);
				}
			}

			public static class PostClaimEvent extends CapitolBlockPlacedEvent {

				@Internal
				public PostClaimEvent(
					CapitolBlock block,
					Team team,
					Player player,
					BlockPos pos,
					Level level
				) {
					super(
						block,
						team,
						player,
						pos,
						level,
						Stage.POST
					);
				}
			}

			@Cancelable
			public static class FailedToPlaceEvent extends CapitolBlockPlacedEvent {

				@Internal
				public FailedToPlaceEvent(
					CapitolBlock block,
					Player player,
					BlockPos pos,
					Level level
				) {
					super(
						block,
						null,
						player,
						pos,
						level,
						Stage.FAILED
					);
				}

				@Override
				@Nullable
				public Team getTeam() {
					return null;
				}
			}
		}

		public static class CapitolBlockDestroyedEvent extends CapitolBlockDestroyedOrPlaced {

			@Internal
			public CapitolBlockDestroyedEvent(
				CapitolBlock block,
				Team team,
				Player player,
				BlockPos pos,
				Level level,
				Stage stage
			) {
				super(
					block,
					team,
					player,
					pos,
					level,
					stage,
					false
				);
			}

			public static class PreUnclaimEvent extends CapitolBlockPlacedEvent {

				@Internal
				public PreUnclaimEvent(
					CapitolBlock block,
					Team team,
					Player player,
					BlockPos pos,
					Level level
				) {
					super(
						block,
						team,
						player,
						pos,
						level,
						Stage.PRE
					);
				}
			}

			public static class PostUnclaimEvent extends CapitolBlockPlacedEvent {

				@Internal
				public PostUnclaimEvent(
					CapitolBlock block,
					Team team,
					Player player,
					BlockPos pos,
					Level level
				) {
					super(
						block,
						team,
						player,
						pos,
						level,
						Stage.POST
					);
				}
			}

			@Cancelable
			public static class FailedToDestroy extends CapitolBlockPlacedEvent {

				@Internal
				public FailedToDestroy(
					CapitolBlock block,
					Player player,
					BlockPos pos,
					Level level
				) {
					super(
						block,
						null,
						player,
						pos,
						level,
						Stage.FAILED
					);
				}

				@Override
				@Nullable
				public Team getTeam() {
					return null;
				}
			}
		}
	}
}