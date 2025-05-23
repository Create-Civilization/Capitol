package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.event.custom.WarEvent;
import com.createcivilization.capitol.team.*;
import com.createcivilization.capitol.util.*;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.blending.BlendingData;

import net.neoforged.neoforge.common.NeoForge;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ChunkAccess.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class ChunkDataImpl implements IChunkData {

	@Shadow
	public abstract Level getLevel();

	@Shadow
	public abstract ChunkPos getPos();

	@Unique
	private int takeOverProgress = 0;

	@Unique
	private final ServerBossEvent takeOverBar = new ServerBossEvent(Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onConstruct(
		ChunkPos chunkPos,
		UpgradeData upgradeData,
		LevelHeightAccessor levelHeightAccessor,
		Registry<Biome> biomeRegistry,
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
	 * Sets the {@link #takeOverProgress} to the provided parameter.
	 * @param i The new takeover progress.
	 */
	@Override
	public void setTakeOverProgress(int i) {
		this.takeOverProgress = i;
	}

	@Override
	public void resetTakeOverProgress() {
		this.setTakeOverProgress(0);
	}

	@Override
	public void incrementTakeOverProgress(double modifier) {
		takeOverBar.setColor(BossEvent.BossBarColor.RED);
		this.setTakeOverProgress((int) (this.getTakeOverProgress() + (CapitolConfig.SERVER.warTakeoverIncrement.get() * modifier)));
	}

	@Override
	public void decrementTakeOverProgress(double modifier) {
		if (this.getTakeOverProgress() <= 0) {
			this.resetTakeOverProgress();
			return;
		}
		takeOverBar.setColor(BossEvent.BossBarColor.BLUE);
		this.setTakeOverProgress((int) (this.getTakeOverProgress() - (CapitolConfig.SERVER.warTakeoverDecrement.get() * modifier)));
	}

	// I don't find this funny, it's just annoying

	@Override
	public void updateTakeOverProgress(MinecraftServer server) {
		ChunkAccess chunkAccess = this.$();
		if (!TeamUtils.isChunkEdgeOfClaims(chunkAccess)) return;
		ChunkPos pos = this.getPos();
		ResourceLocation dimension = this.getLevel().dimension().location();
		Team team = TeamUtils.getTeam(pos, dimension).getOrThrow();
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

		// Nobody is in chunk, decay
		if (!anyInChunk) this.decrementTakeOverProgress(CapitolConfig.SERVER.warDecayMultiplier.get());
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
				dimension,
				pos
			);
			this.resetTakeOverProgress();
			NeoForge.EVENT_BUS.post(new WarEvent.ChunkTakenOverEvent(chunkAccess, team));
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
	private ChunkAccess $() {
		return (ChunkAccess) (Object) this;
	}
}