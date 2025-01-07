package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.team.War;
import com.createcivilization.capitol.util.*;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.chunk.ChunkAccess;

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

	@Override
	public float getTakeOverProgress() {
		return this.takeOverProgress;
	}

	@Override
	public void setTakeOverProgress(float i) {
		this.takeOverProgress = i;
	}

	@Override
	public void incrementTakeOverProgress() {
		this.setTakeOverProgress(this.takeOverProgress += Config.warTakeoverIncrement.getOrThrow());
	}

	@Override
	@SuppressWarnings("DataFlowIssue")
	public void updateTakeOverProgress(MinecraftServer server) {
		for (War war : TeamUtils.wars) {
			if (TeamUtils.isChunkEdgeOfClaims((ChunkAccess) (Object) this)) {

				var players = server.getPlayerList().getPlayers();
				boolean isThisChunkClaimedByDeclaringTeam =
					TeamUtils.getTeam(this.getPos(), this.getThisLevel().dimension().location()).getOrThrow().equals(war.getDeclaringTeam());

				if (players.stream().anyMatch((player) -> this.isPlayerInChunkAndEnemy(player, war, isThisChunkClaimedByDeclaringTeam))) {
					if (this.getTakeOverProgress() < Config.maxWarTakeoverAmount.getOrThrow()) this.incrementTakeOverProgress();
					else {
						TeamUtils.unclaimChunk(
							isThisChunkClaimedByDeclaringTeam ? war.getDeclaringTeam() : war.getReceivingTeam(),
							this.getThisLevel().dimension().location(),
							this.getPos()
						);
					}
				}
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