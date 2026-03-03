package com.createcivilization.capitol.old.server.mixin;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.common.data.ClaimData;
import com.createcivilization.capitol.old.common.data.TeamData;
import com.createcivilization.capitol.old.old.util.data.DataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

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
		 * This mixin also sets {@link com.createcivilization.capitol.old.server.ServerConstants#SERVER} to be the server instance.
		 */
        @Inject(at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/ModConfigSpec$BooleanValue;get()Ljava/lang/Object;", shift = At.Shift.BEFORE), method = "initServer")
        public void loadTeams(CallbackInfoReturnable<Boolean> cir) {
			Capitol.LOGGER.info("Loading capitol data..");
			com.createcivilization.capitol.old.server.ServerConstants.SERVER.set((MinecraftServer) (Object) this);
			try {
				TeamData.DataUtils.loadData();
				ClaimData.DataUtils.loadData();
			} catch (IOException e) {
				throw new RuntimeException("An error occurred trying to load teams for Capitol!", e);
			}
			Capitol.LOGGER.info("Capitol data loaded successfully.");
        }
    }

	@Mixin(MinecraftServer.class)
	public abstract static class DataSaverImpl {

		/**
		 * Saves the teams every time /save-all or the autosave feature runs.
		 */
		@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;saveAll()V", shift = At.Shift.BEFORE), method = "saveEverything")
		private void autoSaveTeams(boolean suppressLog, boolean flush, boolean forced, CallbackInfoReturnable<Boolean> cir) {
			capitol$saveData();
		}

		/**
		 * Saves the teams when the server stops, right before the player list is saved and cleared.
		 */
		@Inject(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 1), method = "stopServer")
		private void saveTeams(CallbackInfo ci) {
			capitol$saveData();
		}

		@Unique
		private static void capitol$saveData() {
			Capitol.LOGGER.info("Saving capitol data..");
			try {
				TeamData.DataUtils.saveData();
				ClaimData.DataUtils.saveData();
			} catch (IOException e) {
				throw new RuntimeException("An error occurred trying to save teams for Capitol!", e);
			}
			Capitol.LOGGER.info("Capitol data saved successfully.");
		}
	}
}