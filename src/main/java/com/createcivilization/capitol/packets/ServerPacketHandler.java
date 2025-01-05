package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.api.distmarker.*;

import java.awt.*;

@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerPacketHandler {

	public static void syncDataWithPlayer(ServerPlayer sender) {
		TeamUtils.synchronizeServerDataWithPlayer(sender);
	}

    public static void createTeam(String teamName, ServerPlayer sender, Color teamColor) {
		if (TeamUtils.hasTeam(sender)) return;
		TeamUtils.loadedTeams.add(TeamUtils.createTeam(teamName, sender, teamColor));
    }
}