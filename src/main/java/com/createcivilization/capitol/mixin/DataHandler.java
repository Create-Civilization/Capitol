package com.createcivilization.capitol.mixin;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.io.*;
import java.util.function.BooleanSupplier;

/**
 * Class for saving and loading Team data and claimed Chunks.<br>
 * The data is loaded before {@link net.minecraftforge.event.server.ServerStartingEvent}, and the teams are stored in {@link TeamUtils#loadedTeams}.<br>
 * The data is saved regularly on the autosave (/save-all), and saved and cleared when the server stops, right before the player list is saved and cleared.
 */
@SuppressWarnings("DiscouragedShift")
public final class DataHandler {

    @Mixin(MinecraftDedicatedServer.class)
    public abstract static class DataLoaderImpl {

		/**
		 * Loads the teams when the server starts.<br>
		 * This mixin also sets {@link Capitol#server} to be the server instance.
		 */
        @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;get()Ljava/lang/Object;", shift = At.Shift.BEFORE), method = "setupServer")
        public void loadTeams(CallbackInfoReturnable<Boolean> cir) throws IOException {
			Config.loadConfig();
			Capitol.server.set((MinecraftServer) (Object) this);
            TeamUtils.loadTeams();
        }
    }

	@Mixin(MinecraftServer.class)
	public abstract static class DataSaverImpl {

		/**
		 * Saves the teams every time /save-all or the autosave feature runs.
		 */
		@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;saveAllPlayerData()V", shift = At.Shift.BEFORE), method = "saveAll")
		private void autoSaveTeams(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) throws IOException {
			Config.saveConfig();
			TeamUtils.saveTeams();
		}

		/**
		 * Saves the teams when the server stops, right before the player list is saved and cleared.
		 */
		@Inject(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 1), method = "shutdown")
		private void saveTeams(CallbackInfo ci) throws IOException {
			Config.saveConfig();
			TeamUtils.saveTeams();
		}
	}

	@Mixin(MinecraftServer.class)
	public abstract static class ChunkDataImplImpl {

		@Shadow
		public abstract Iterable<ServerWorld> getWorlds();

		// Before any tick do:
		@Inject(at = @At(value = "HEAD"), method = "tick")
		private void updateTakeOverProgress(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
			for (Team team : TeamUtils.loadedTeams) {
				for (var recLoc : team.getClaimedChunks().keySet()) {
					for (ServerWorld level : this.getWorlds()) {
						if (level.getRegistryKey().getValue().equals(recLoc)) {
							for (var chunkPos : team.getClaimedChunks().get(recLoc)) {
								((IChunkData) level.getChunk(chunkPos.getStartPos())).updateTakeOverProgress();
							}
						}
					}
				}
			}
		}
	}
}