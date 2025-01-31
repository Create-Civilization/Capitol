package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.event.custom.WarEvent;
import com.createcivilization.capitol.team.War;
import com.createcivilization.capitol.util.*;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;

import net.minecraftforge.common.MinecraftForge;

import org.spongepowered.asm.mixin.*;

import javax.annotation.Nullable;

@Mixin(ChunkAccess.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class ChunkDataImpl implements IChunkData {

	@Shadow
	@Nullable
	public abstract LevelAccessor getWorldForge();

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

	@Override
	public void updateTakeOverProgress(MinecraftServer server) {
		for (War war : TeamUtils.wars) {
			if (TeamUtils.isChunkEdgeOfClaims((ChunkAccess) (Object) this)) {
				if (this.getTakeOverProgress() < 0) {
					String msg = "ERROR: Takeover progress is less than 0! Error occurred at ChunkPos " + this.getPos();
					System.out.println(msg);
					LogToDiscord.postIfAllowed(
						"Capitol",
						msg
					);
					this.resetTakeOverProgress();
				}

				var players = server.getPlayerList().getPlayers();
				var team = TeamUtils.getTeam(this.getPos(), this.getThisLevel().dimension().location()).getOrThrow();
				boolean isThisChunkClaimedByDeclaringTeam = team.equals(war.getDeclaringTeam());
				if (players.stream().anyMatch((player) -> this.isPlayerInChunkAndEnemy(player, war, isThisChunkClaimedByDeclaringTeam))) {
					if (this.getTakeOverProgress() <= CapitolConfig.SERVER.maxWarTakeoverAmount.get()) this.incrementTakeOverProgress();
					else {
						var thisTeam = isThisChunkClaimedByDeclaringTeam ? war.getReceivingTeam() : war.getDeclaringTeam();
						TeamUtils.unclaimChunkAndUpdate(
							thisTeam,
							this.getThisLevel().dimension().location(),
							this.getPos()
						);
						this.resetTakeOverProgress();
						//noinspection DataFlowIssue
						MinecraftForge.EVENT_BUS.post(new WarEvent.ChunkTakenOverEvent(war, (ChunkAccess)(Object)this, thisTeam));
						LogToDiscord.postIfAllowed(
							team,
							"Chunk taken over in war " + war + ", at ChunkPos " + this.getPos()
						);
					}
				} else if (this.wasJustIncremented || this.isDecrementing) this.decrementTakeOverProgress();
			}
		}
	}

	@Unique
	public Level getThisLevel() {
		return (Level) this.getWorldForge();
	}

	@Unique
	public boolean isPlayerInChunkAndEnemy(Player player, War war, boolean isThisChunkClaimedByDeclaringTeam) {
		var uuid = player.getUUID();
		var firstTeamAndTheirAlliesUUIDs = isThisChunkClaimedByDeclaringTeam ? war.getDeclaringTeamAndAlliesUUIDs() : war.getReceivingTeamAndAlliesUUIDs();
		var secondTeamAndTheirAlliesUUIDs = isThisChunkClaimedByDeclaringTeam ? war.getReceivingTeamAndAlliesUUIDs() : war.getDeclaringTeamAndAlliesUUIDs();
		boolean playerIsNotOfThisTeamOrTheirAllies = !firstTeamAndTheirAlliesUUIDs.contains(uuid);
		boolean playerIsOfOppositeTeamOrTheirAllies = secondTeamAndTheirAlliesUUIDs.contains(uuid);
		return player.chunkPosition().equals(this.getPos()) && playerIsNotOfThisTeamOrTheirAllies && playerIsOfOppositeTeamOrTheirAllies;
	}
}