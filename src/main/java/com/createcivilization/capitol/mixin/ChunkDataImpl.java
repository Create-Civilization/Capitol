package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.event.custom.WarEvent;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.team.War;
import com.createcivilization.capitol.util.*;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.blending.BlendingData;

import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Mixin(ChunkAccess.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class ChunkDataImpl implements IChunkData {

	@Shadow
	@Nullable
	public abstract Level getLevel();

	@Shadow
	public abstract ChunkPos getPos();

	@Unique
	private int takeOverProgress = 0;

	@Unique
	private final ServerBossEvent takeOverBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

	// You removed the usage of these fields? I'm relatively sure I had those as contingencies for something - Mavity
	@Unique
	private boolean
		wasJustIncremented = false,
		isDecrementing = false;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onConstruct(
		ChunkPos chunkPos,
		UpgradeData upgradeData,
		LevelHeightAccessor levelHeightAccessor,
		Registry<Biome> biomeRegistry, // Please for the love of god learn how to use Generics @Orion - Mavity
		long inhabitedTime,
		LevelChunkSection[] sections,
		BlendingData blendingData,
		CallbackInfo ci
	) {
		takeOverBar.setName(Component.literal(chunkPos.x + " " + chunkPos.z));
	}

	/**
	 * @return {@link #takeOverProgress}.
	 */
	@Override
	public int getTakeOverProgress() {
		return this.takeOverProgress;
	}

	/**
	 * Sets the {@link #takeOverProgress} to the provided parameter, resetting both {@link #wasJustIncremented} and {@link #isDecrementing}.
	 * @param i The new takeover progress.
	 */
	@Override
	public void setTakeOverProgress(int i) {
		this.takeOverProgress = i;
		this.wasJustIncremented = false;
		this.isDecrementing = false;
	}

	@Override
	public void resetTakeOverProgress() {
		this.setTakeOverProgress(0);
	}

	@Override
	public void incrementTakeOverProgress(int modifier) {
		takeOverBar.setColor(BossEvent.BossBarColor.RED);
		this.setTakeOverProgress(this.getTakeOverProgress() + (CapitolConfig.SERVER.warTakeoverIncrement.get() * modifier));
		this.wasJustIncremented = true;
		this.isDecrementing = false;
	}

	@Override
	public void decrementTakeOverProgress(int modifier) {
		if (this.getTakeOverProgress() <= 0) {
			this.resetTakeOverProgress();
			return;
		}
		takeOverBar.setColor(BossEvent.BossBarColor.BLUE);
		this.setTakeOverProgress(this.getTakeOverProgress() - (CapitolConfig.SERVER.warTakeoverDecrement.get() * modifier));
		this.wasJustIncremented = false;
		this.isDecrementing = this.getTakeOverProgress() != 0;
	}

	//TODO: FIX TEAM HANG ON UNCLAIMED CHUNKS
	//TODO: FIX IMPROPER ENEMY DETECTION

	@Override
	public void updateTakeOverProgress(MinecraftServer server) {
		if (!TeamUtils.isChunkEdgeOfClaims(this.$())) return;
		ChunkPos pos = this.getPos();
		// Inlined because we only use the dimension ResourceLocation once - Mavity
		//noinspection DataFlowIssue
		Team team = TeamUtils.getTeam(pos, this.getLevel().dimension().location()).getOrThrow();
		PlayerList serverPlayerList = server.getPlayerList();

		int balance = 0;
		boolean anyInChunk = false;

		for (War loadedWar : TeamUtils.loadedWars) {
			boolean isDeclaring = loadedWar.getDeclaringTeam().getTeamId().equals(team.getTeamId());
			List<ServerPlayer> enemiesInChunk = getMembersOfSideInChunkAndAddToBar(serverPlayerList, (isDeclaring ? loadedWar.getReceivingTeamAndAlliesUUIDs() : loadedWar.getDeclaringTeamAndAlliesUUIDs()));
			List<ServerPlayer> alliesInChunk = getMembersOfSideInChunkAndAddToBar(serverPlayerList, (!isDeclaring ? loadedWar.getReceivingTeamAndAlliesUUIDs() : loadedWar.getDeclaringTeamAndAlliesUUIDs()));

			anyInChunk = anyInChunk || (!enemiesInChunk.isEmpty() || !alliesInChunk.isEmpty());
			if (!anyInChunk) continue;

			balance += enemiesInChunk.size() - alliesInChunk.size();
		}

		// Nobody is in chunk
		if (!anyInChunk) this.decrementTakeOverProgress(1);
		// Someone is in the chunk past this
		// Contested
		else if (balance == 0) takeOverBar.setColor(BossEvent.BossBarColor.WHITE);
		// Allies in chunk
		else if (balance < 0) this.decrementTakeOverProgress(balance);
		// Enemies in chunk
		else this.incrementTakeOverProgress(balance);

		int maxTakeOver = CapitolConfig.SERVER.maxWarTakeoverAmount.get();

		if (this.getTakeOverProgress() >= maxTakeOver) {
			takeOverBar.removeAllPlayers();
			TeamUtils.unclaimChunk(
				team,
				this.getLevel().dimension().location(),
				pos
			);
			this.resetTakeOverProgress();
			NeoForge.EVENT_BUS.post(new WarEvent.ChunkTakenOverEvent(this.$(), team));
			LogToDiscord.postIfAllowed(
				team,
				"Chunk taken from team " + team.getName() +" over in war , at ChunkPos " + pos
			);
		}

		takeOverBar.setProgress((float) this.getTakeOverProgress() / maxTakeOver);
	}

	@Unique
	private List<ServerPlayer> getMembersOfSideInChunkAndAddToBar(PlayerList playerList, List<UUID> uuids) {
		return uuids.stream().map(playerList::getPlayer).filter(Objects::nonNull).filter(serverPlayer -> {
			boolean isInChunk = serverPlayer.chunkPosition().equals(this.getPos());
			if (isInChunk) takeOverBar.addPlayer(serverPlayer);
			else takeOverBar.removePlayer(serverPlayer);
			return isInChunk;
		}).toList();
	}

	/**
	 * @return {@code this as Object as ChunkAccess}.
	 */
	@Unique
	public ChunkAccess $() {
		return (ChunkAccess) (Object) this;
	}
}