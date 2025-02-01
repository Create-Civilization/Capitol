package com.createcivilization.capitol.constants;

import com.createcivilization.capitol.team.Team;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import net.minecraftforge.api.distmarker.*;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientConstants {

	public static final Minecraft INSTANCE = Minecraft.getInstance();
	public static boolean viewChunks;
	public static final ObjectHolder<Team> playerTeam = new ObjectHolder<>();
	public static boolean teamChat = false;

	// JourneyMap Integration use ONLY
	public static boolean chunksDirty = false;
	public static final List<String> toResetChunksTeamIds = new ArrayList<>();

	public static final Component NOT_IN_TEAM = Component.literal("You are not in a team");
	public static final Component NOT_NEAR_CHUNK = Component.literal("Must be next to a claimed chunk to do this");
	public static final Component CHUNK_ALREADY_CLAIMED = Component.literal("Chunk already claimed");
	public static final Component CHUNK_SUCCESSFULLY_CLAIMED = Component.literal("Chunk successfully claimed");

}