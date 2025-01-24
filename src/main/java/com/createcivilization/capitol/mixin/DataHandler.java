package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.constants.ServerConstants;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.io.*;
import java.util.function.BooleanSupplier;

/**
 * Class for saving and loading Team data and claimed chunks.<br>
 * The data is loaded before {@link net.minecraftforge.event.server.ServerStartingEvent}, and the teams are stored in {@link TeamUtils#loadedTeams}.<br>
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
        @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;get()Ljava/lang/Object;", shift = At.Shift.BEFORE), method = "initServer")
        public void loadTeams(CallbackInfoReturnable<Boolean> cir) {
			ServerConstants.server.set((MinecraftServer) (Object) this);
			try {
				TeamUtils.loadTeams();
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
				TeamUtils.saveTeams();
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
				TeamUtils.saveTeams();
			} catch (IOException e) {
				throw new RuntimeException("An error occurred trying to save teams for Capitol!", e);
			}
		}
	}

	@Mixin(MinecraftServer.class)
	public abstract static class ChunkDataImplImpl {

		@Shadow
		public abstract Iterable<ServerLevel> getAllLevels();

		// Before any tick do:
		@Inject(at = @At(value = "HEAD"), method = "tickServer")
		private void updateTakeOverProgress(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
			for (Team team : TeamUtils.loadedTeams) {
				for (var recLoc : team.getClaimedChunks().keySet()) {
					for (ServerLevel level : this.getAllLevels()) {
						if (level.dimension().location().equals(recLoc)) {
							for (var chunkPos : team.getClaimedChunks().get(recLoc)) {
								((IChunkData) level.getChunk(chunkPos.getWorldPosition())).updateTakeOverProgress((MinecraftServer) (Object) this);
							}
						}
					}
				}
			}
		}
	}
}