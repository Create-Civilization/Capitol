package com.createcivilization.capitol.server.invites;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteHandler {

	private static final Map<Player, List<Team>> invites = new HashMap<>();

	public static void addInvite(Player player, Team team){
		CapitolDatabase database = DatabaseManager.database;
		if(database.getPlayerTeam(player) != null) return;
		List<Team> playerInvites = invites.getOrDefault(player, new ArrayList<>());
		playerInvites.add(team);
		invites.put(player, playerInvites);

		String acceptCmd = "/capitol invites " + team.getName() + " accept";
		String denyCmd   = "/capitol invites " + team.getName() + " deny";

		MutableComponent accept = Component.literal("[Accept]")
			.withStyle(s -> s.withColor(ChatFormatting.GREEN)
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, acceptCmd))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(acceptCmd))));

		MutableComponent deny = Component.literal("[Deny]")
			.withStyle(s -> s.withColor(ChatFormatting.RED)
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, denyCmd))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(denyCmd))));

		player.sendSystemMessage(Component.literal("You have been invited to join ").withStyle(ChatFormatting.YELLOW)
			.append(Component.literal(team.getName()).withStyle(ChatFormatting.GOLD))
			.append(Component.literal("! ").withStyle(ChatFormatting.YELLOW))
			.append(accept)
			.append(Component.literal(" ").withStyle(ChatFormatting.WHITE))
			.append(deny));
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
