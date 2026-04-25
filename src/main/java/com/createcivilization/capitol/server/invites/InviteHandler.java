package com.createcivilization.capitol.server.invites;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteHandler {

	private static Map<Player, List<Team>> invites = new HashMap<>();

	public static void addInvite(Player player, Team team){
		CapitolDatabase database = DatabaseManager.database;
		if(database.getPlayerTeam(player) != null) return;
		List<Team> playerInvites = invites.getOrDefault(player, new ArrayList<>());
		playerInvites.add(team);
		invites.put(player, playerInvites);

		//TODO NOTIFY PLAYER OF INVITE SEND PACKET
	}

	public static void removeInvite(Player player, Team team){
		List<Team> playerInvites = invites.getOrDefault(player, new ArrayList<>());
		if(playerInvites.isEmpty()) return;
		playerInvites.remove(team);
	}

	public static void clearInvites(Player player){
		invites.remove(player);
	}

	public static List<Team> getInvites(Player player){
		List<Team> playerInvites = invites.getOrDefault(player, new ArrayList<>());
		if (playerInvites.isEmpty()) return null;
		return playerInvites;
	}

}
