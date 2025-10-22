package com.createcivilization.capitol.common.data;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.assets.Request;
import com.createcivilization.capitol.common.assets.Status;
import com.createcivilization.capitol.server.ServerConstants;
import com.createcivilization.capitol.server.utils.GsonUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Data manager for all relevant claim data.
 */
public class ClaimData {

	private static final Map<UUID, Map<ResourceLocation, List<ChunkPos>>> CHUNKS = new HashMap<>();
	public static final File DATA_FILE = new File(ServerConstants.CAPITOL_FOLDER, "chunk_data.json");

	/**
	 * Utilities that return a Status and perform checks and higher level actions.
	 */
	public static class SmartUtils {
		public static Status claimChunk(Request request, UUID teamId, ResourceLocation dimension, ChunkPos chunkPos) {
			UUID origin = request.origin();
			if (!origin.equals(ServerConstants.SERVER_UUID) && !TeamData.BaseUtils.hasPermission(origin, "claimChunks"))
				return new Status(
					false,
					"Insufficient permissions to claim a chunk in this team",
					null
				);

			CHUNKS.get(teamId).computeIfAbsent(dimension, dim -> new ArrayList<>()).add(chunkPos);

			return new Status(
				true,
				"Chunk successfully claimed.",
				null
			);
		}
	}

	/**
	 * Utilities that perform basic actions and checks without returning a Status.
	 */
	public static class BaseUtils {

		public static void setChunks(Map<UUID, Map<ResourceLocation, List<ChunkPos>>> chunkPos) {
			CHUNKS.clear();
			CHUNKS.putAll(chunkPos);
		}

		public static String getChunks() {
			return CHUNKS.toString();
		}

		public static void startTeam(UUID teamId) {
			CHUNKS.put(teamId, new HashMap<>());
		}
	}

	/**
	 * Utilities that manage chunks data wise
	 */
	public static class DataUtils {
		public static void loadData() throws IOException {
			if (!ServerConstants.CAPITOL_FOLDER.exists() || !DATA_FILE.exists()) return;
			Capitol.LOGGER.info("Loading capitol data..");
			FileReader fileReader = new FileReader(DATA_FILE);
			StringBuilder string = new StringBuilder();

			int charInt;

			while ((charInt = fileReader.read()) != -1) {
				string.append((char) charInt);
			}

			ClaimData.BaseUtils.setChunks(GsonUtil.deserializeChunkMap(string.toString()));

			Capitol.LOGGER.info("Capitol data loaded successfully.");
		}

		public static void saveData() throws IOException {
			Capitol.LOGGER.info("Saving capitol data..");
			if (!ServerConstants.CAPITOL_FOLDER.exists()) if (!ServerConstants.CAPITOL_FOLDER.mkdir()) throw new IOException("Capitol Data folder could not be made.");
			if (!DATA_FILE.exists()) {
				if (!DATA_FILE.createNewFile()) throw new IOException("Team Data file could not be made.");
				else {
					DATA_FILE.setWritable(true);
					DATA_FILE.setReadable(true);
				}
			}

			FileWriter fileWriter = new FileWriter(DATA_FILE);
			fileWriter.write(GsonUtil.serializeChunkMap(CHUNKS));
			fileWriter.close();
			Capitol.LOGGER.info("Capitol data saved successfully.");
		}
	}
}
