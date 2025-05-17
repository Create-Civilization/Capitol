package com.createcivilization.capitol.constants;

import com.createcivilization.capitol.team.Team;

import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

import java.util.*;

public class ClientConstants {

	public static final Minecraft INSTANCE = Minecraft.getInstance();
	public static boolean viewChunks;
	public static final ObjectHolder<Team> playerTeam = new ObjectHolder<>();
	public static boolean teamChat = false;
	public static int lastPage, lastTab = 0;

	// JourneyMap Integration use ONLY
	public static boolean chunksDirty = false;
	public static final List<String> toResetChunksTeamIds = new ArrayList<>();

	// ERROR
	public static final Component NOT_IN_TEAM = Component.literal("You are not in a team");
	public static final Component NOT_NEAR_CHUNK = Component.literal("Must be next to a claimed chunk to do this");
	public static final Component CHUNK_ALREADY_CLAIMED = Component.literal("Chunk already claimed");

	// SUCCESS
	public static final Component CHUNK_SUCCESSFULLY_CLAIMED = Component.literal("Chunk successfully claimed");
	public static final Component TEAM_SUCCESSFULLY_CREATED = Component.literal("Team successfully created");

	// TITLE
	public static final Component CREATE_TEAM = Component.literal("Create Team");
	public static final Component TEAM_STATISTICS = Component.literal("Team Statistics");

	public static ObjectHolder<Team> getPlayerTeam() {
		playerTeam.setFrom(TeamUtils.getTeam(INSTANCE.player));
		return playerTeam;
	}
}