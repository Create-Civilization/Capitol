package com.createcivilization.capitol.util.data;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.payloads.bidirectional.add.BiAddChunk;
import com.createcivilization.capitol.payloads.bidirectional.add.BiAddTeam;
import com.createcivilization.capitol.payloads.bidirectional.add.BiAddWar;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.team.War;
import com.createcivilization.capitol.util.team.LogToDiscord;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import wiiu.mavity.wiiu_lib.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

	public static class TeamData {

		/**
		 * A list of the currently loaded teams.
		 */
		public static final List<Team> loadedTeams = new ArrayList<>();

		/**
		 * @return A list of {@link Team}s parsed from the given {@link String}.
		 */
		public static List<Team> parseTeams(String str) {
			return GsonUtil.loadTeamsFromString(str);
		}

		/**
		 * Dumps the currently loaded teams, and then loads the teams in the receivingTeam data file.
		 * @return 1 if successful, -1 if not (for /command usage)
		 */
		public static int reloadTeamsFromFile() {
			try {
				loadedTeams.clear();
				WarData.loadedWars.clear();
				loadData();
				return 1;
			} catch (IOException e) {
				e.printStackTrace(System.out);
				e.printStackTrace(System.err);
				return -1;
			}
		}

		/**
		 * Saves the teams to the receivingTeam data file, dumps the receivingTeam list, then reloads the teams.
		 * @return 1 if successful, -1 if not (for /command usage)
		 */
		public static int reloadTeams() {
			try {
				saveData();
				loadedTeams.clear();
				loadData();
				return 1;
			} catch (IOException e) {
				e.printStackTrace(System.out);
				e.printStackTrace(System.err);
				return -1;
			}
		}
	}

	public static class WarData {

		public static final List<War> loadedWars = new ArrayList<>();

		/**
		 * @return A list of {@link War}s parsed from the given {@link String}.
		 */
		public static List<War> parseWars(String str) {
			return GsonUtil.loadWarsFromString(str);
		}
	}

	/**
	 * Loads all the data from their respective files.
	 */
	//TODO:: Can be optimized
    public static void loadData() throws IOException {
		Capitol.LOGGER.info("Loading teams...");
        File file = FileUtil.getTeamDataFile();
		try {
			FileUtils.setContentsIfEmpty(file, "[" + System.lineSeparator() + "]");
		} finally {
			TeamData.loadedTeams.addAll(TeamData.parseTeams(FileUtils.getFileContents(file)));
			LogToDiscord.postIfAllowed("Capitol", "Loaded teams and chunks");
			Capitol.LOGGER.info("Loaded teams successfully");
		}

		Capitol.LOGGER.info("Loading wars...");
		file = FileUtil.getWarDataFile();
		try {
			FileUtils.setContentsIfEmpty(file, "[" + System.lineSeparator() + "]");
		} finally {
			WarData.loadedWars.addAll(WarData.parseWars(FileUtils.getFileContents(file)));
			LogToDiscord.postIfAllowed("Capitol", "Loaded wars");
			Capitol.LOGGER.info("Loaded wars successfully");
		}
    }

	/**
	 * Saves all the data to their respective files.
	 */
    public static void saveData() throws IOException {

		File teamDataFile = FileUtil.getTeamDataFile();
		File warDataFile = FileUtil.getWarDataFile();

		Capitol.LOGGER.info("Saving teams..");

		GsonUtil.saveTeamToFile(TeamData.loadedTeams, teamDataFile.getPath());

		Capitol.LOGGER.info("Saving wars..");

		GsonUtil.saveWarToFile(WarData.loadedWars, warDataFile.getPath());

		LogToDiscord.postIfAllowed("Capitol", "Saved teams and wars");
    }

	public static void synchronizeServerDataWithPlayer(ServerPlayer player) {
		TeamData.loadedTeams.forEach(team -> {
			PacketHandler.sendToPlayer(new BiAddTeam(team), player);
			team.getDimensionDataMap().forEach((key, value) -> value.getCapitolDataList().forEach(capitolData -> capitolData.getChildChunks().forEach(chunkPos -> PacketHandler.sendToPlayer(new BiAddChunk(chunkPos, team.getTeamId(), key), player))));
		});

		WarData.loadedWars.forEach(war ->
			PacketHandler.sendToPlayer(new BiAddWar(war.getDeclaringTeam(), war.getReceivingTeam()), player));
	}
}
