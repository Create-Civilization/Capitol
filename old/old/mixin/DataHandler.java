package com.createcivilization.capitol.old.old.mixin;

import com.createcivilization.capitol.old.old.constants.ServerConstants;
import com.createcivilization.capitol.old.old.team.OldTeam;

import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.old.util.data.IChunkData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.io.*;
import java.util.function.BooleanSupplier;

/**
 * Class for saving and loading OldTeam data and claimed chunks.<br>
 * The data is loaded before {@link net.neoforged.neoforge.event.server.ServerStartingEvent}, and the teams are stored in {@link DataManager.TeamData#LOADED_OLD_TEAMS}.<br>
 * The data is saved regularly on the autosave (/save-all), and saved and cleared when the server stops, right before the player list is saved and cleared.
 */
@SuppressWarnings("DiscouragedShift")
public final class DataHandler {

    @Mixin(DedicatedServer.class)
    public abstract static class DataLoaderImpl {

		/**
		 * Loads the teams when the server starts.<br>
		 * This mixin also sets {@link ServerConstants#server} to be the server instance.
		 */
        @Inject(at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/ModConfigSpec$BooleanValue;get()Ljava/lang/Object;", shift = At.Shift.BEFORE), method = "initServer")
        public void loadTeams(CallbackInfoReturnable<Boolean> cir) {
			ServerConstants.server.set((MinecraftServer) (Object) this);
			try {
				DataManager.loadData();
			} catch (IOException e) {
				throw new RuntimeException("An error occurred trying to load teams for Capitol!", e);
			}
        }
    }

	@Mixin(MinecraftServer.class)
	public abstract static class DataSaverImpl {

		/**
		 * Saves the teams every time /save-all or the autosave feature runs.
		 */
		@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;saveAll()V", shift = At.Shift.BEFORE), method = "saveEverything")
		private void autoSaveTeams(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
			try {
				DataManager.saveData();
			} catch (IOException e) {
				throw new RuntimeException("An error occurred trying to save teams for Capitol!", e);
			}
		}

		/**
		 * Saves the teams when the server stops, right before the player list is saved and cleared.
		 */
		@Inject(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 1), method = "stopServer")
		private void saveTeams(CallbackInfo ci) {
			try {
				DataManager.saveData();
			} catch (IOException e) {
				throw new RuntimeException("An error occurred trying to save teams for Capitol!", e);
			}
		}
	}

	@Mixin(MinecraftServer.class)
	public abstract static class ChunkDataImplImpl {

		@Shadow
		public abstract Iterable<ServerLevel> getAllLevels();

		@Inject(at = @At(value = "HEAD"), method = "tickServer")
		private void updateTakeOverProgress(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
			for (OldTeam oldTeam : DataManager.TeamData.LOADED_OLD_TEAMS) {
				for (ResourceLocation recLoc : oldTeam.getDimensionDataMap().keySet()) {
					for (ServerLevel level : this.getAllLevels()) {
						if (level.dimension().location().equals(recLoc)) {
							for (ChunkPos chunkPos : oldTeam.getDimensionalData(recLoc).getAllChildChunks()) {
								((IChunkData) level.getChunk(chunkPos.getWorldPosition())).updateTakeOverProgress((MinecraftServer) (Object) this);
							}
						}
					}
				}
			}
		}
	}
}