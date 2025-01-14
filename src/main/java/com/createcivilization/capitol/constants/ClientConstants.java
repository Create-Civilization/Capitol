package com.createcivilization.capitol.constants;

import com.createcivilization.capitol.team.Team;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import net.minecraftforge.api.distmarker.*;

import wiiu.mavity.util.ObjectHolder;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientConstants {

	public static final Minecraft INSTANCE = Minecraft.getInstance();
	public static boolean viewChunks;
	public static ObjectHolder<Team> playerTeam;
	public static boolean teamChat = false;
	public static boolean chunksDirty = false;

	public static final Component NOT_IN_TEAM = Component.literal("You are not in a team");
	public static final Component NOT_NEAR_CHUNK = Component.literal("You must be next to a claimed chunk to do this");
	public static final Component CHUNK_ALREADY_CLAIMED = Component.literal("Chunk already claimed");
	public static final Component CHUNK_SUCCESSFULLY_CLAIMED = Component.literal("Chunk successfully claimed");

}