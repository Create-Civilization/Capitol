package com.createcivilization.capitol.constants;

import com.createcivilization.capitol.team.Team;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import wiiu.mavity.util.ObjectHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientConstants {
	public static final Minecraft INSTANCE = Minecraft.getInstance();
	public static boolean viewChunks;
	public static Map<UUID, String> playerMap = new HashMap<>();
	public static ObjectHolder<Team> playerTeam;
}
