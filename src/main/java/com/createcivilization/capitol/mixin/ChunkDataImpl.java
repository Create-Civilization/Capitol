package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.event.custom.WarEvent;
import com.createcivilization.capitol.team.War;
import com.createcivilization.capitol.util.*;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;

import net.neoforged.neoforge.common.NeoForge;

import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;

@Mixin(ChunkAccess.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class ChunkDataImpl implements IChunkData {

	@Shadow
	@Nullable
	public abstract Level getLevel();

	@Shadow
	public abstract ChunkPos getPos();

	@Unique
	private float takeOverProgress = 0;

	@Unique
	private boolean
		wasJustIncremented = false,
		isDecrementing = false;

	@Override
	public float getTakeOverProgress() {
		return this.takeOverProgress;
	}

	@Override
	public void setTakeOverProgress(float i) {
		this.takeOverProgress = i;
		this.wasJustIncremented = false;
		this.isDecrementing = false;
	}

	@Override
	public void resetTakeOverProgress() {
		this.setTakeOverProgress(0);
	}

	@Override
	public void incrementTakeOverProgress() {
		this.setTakeOverProgress(this.getTakeOverProgress() + CapitolConfig.SERVER.warTakeoverIncrement.get());
		this.wasJustIncremented = true;
		this.isDecrementing = false;
	}

	@Override
	public void decrementTakeOverProgress() {
		this.setTakeOverProgress(this.getTakeOverProgress() - CapitolConfig.SERVER.warTakeoverDecrement.get());
		this.wasJustIncremented = false;
		this.isDecrementing = this.getTakeOverProgress() != 0;
	}

	//TODO:: FIX TEAM HANG ON UNCLAIMED CHUNKS
	//TODO:: FIX IMPROPER ENEMY DETECTION

	@Override
	@SuppressWarnings("DataFlowIssue")
	public void updateTakeOverProgress(MinecraftServer server) {
		for (War war : TeamUtils.loadedWars) {
			if (!TeamUtils.isChunkEdgeOfClaims(this.$())) return;

			var pos = this.getPos();
			if (this.getTakeOverProgress() < 0) {
				String msg = "ERROR: Takeover progress is less than 0! Error occurred at ChunkPos " + pos;
				System.out.println(msg);
				LogToDiscord.postIfAllowed(
					"Capitol",
					msg
				);
				this.resetTakeOverProgress();
			}

			var players = server.getPlayerList().getPlayers();
			var dimensionResourceLocation = this.getLevel().dimension().location();
			var team = TeamUtils.getTeam(pos, dimensionResourceLocation).getOrThrow();
			boolean isDeclaringTeam = team.equals(war.getDeclaringTeam());
			if (players.stream().anyMatch((player) -> this.isPlayerInChunkAndEnemy(player, war, isDeclaringTeam))) {
				if (this.getTakeOverProgress() <= CapitolConfig.SERVER.maxWarTakeoverAmount.get()) this.incrementTakeOverProgress();
				else {
					var thisTeam = isDeclaringTeam ? war.getReceivingTeam() : war.getDeclaringTeam();
					TeamUtils.unclaimChunkAndUpdate(
						thisTeam,
						dimensionResourceLocation,
						pos
					);
					this.resetTakeOverProgress();
					NeoForge.EVENT_BUS.post(new WarEvent.ChunkTakenOverEvent(war, this.$(), thisTeam));
					LogToDiscord.postIfAllowed(
						team,
						"Chunk taken over in war " + war + ", at ChunkPos " + pos
					);
				}
			} else if (this.wasJustIncremented || this.isDecrementing) this.decrementTakeOverProgress();
		}
	}

	/**
	 * @return This.
	 */
	@Unique
	public ChunkAccess $() { // The fact I can name it this is hilarious
		return (ChunkAccess) (Object) this;
	}

	/**
	 * Checks if the {@code player} is in this chunk, and is of the opposite team.
	 * @param player The player to check against.
	 * @param war The war instance.
	 * @param isDeclaringTeam If this chunk is claimed by the declaring team of the war.
	 * @return If the {@code player} is in this chunk, and is of the opposite team.
	 */
	@Unique
	public boolean isPlayerInChunkAndEnemy(Player player, War war, boolean isDeclaringTeam) {
		var uuid = player.getUUID();
		var firstTeamAndTheirAlliesUUIDs =
			isDeclaringTeam ? war.getDeclaringTeamAndAlliesUUIDs() : war.getReceivingTeamAndAlliesUUIDs();
		var secondTeamAndTheirAlliesUUIDs =
			isDeclaringTeam ? war.getReceivingTeamAndAlliesUUIDs() : war.getDeclaringTeamAndAlliesUUIDs();
		boolean playerIsNotOfThisTeamOrTheirAllies = !firstTeamAndTheirAlliesUUIDs.contains(uuid);
		boolean playerIsOfOppositeTeamOrTheirAllies = secondTeamAndTheirAlliesUUIDs.contains(uuid);
		return player.chunkPosition().equals(this.getPos()) && playerIsNotOfThisTeamOrTheirAllies && playerIsOfOppositeTeamOrTheirAllies;
	}
}