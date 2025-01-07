package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.event.ServerEvents;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.api.distmarker.*;
import wiiu.mavity.util.ObjectHolder;

import java.awt.*;
import java.util.Objects;
import java.util.UUID;

@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerPacketHandler {

	public static void syncDataWithPlayer(ServerPlayer sender) {
		TeamUtils.synchronizeServerDataWithPlayer(sender);
	}

    public static void createTeam(String teamName, ServerPlayer sender, Color teamColor) {
		if (TeamUtils.hasTeam(sender)) return;
		TeamUtils.loadedTeams.add(TeamUtils.createTeam(teamName, sender, teamColor));
    }

	public static void claimCurrentPlayerChunk(ServerPlayer sender) {
		if (TeamUtils.isInClaimedChunk(sender) || !TeamUtils.nearClaimedChunk(sender.chunkPosition(), 1, sender)) return;

		TeamUtils.claimCurrentChunk(sender);
	}

	public static void invitePlayerToTeam(ServerPlayer sender, UUID playerToInviteUUID) {
		ObjectHolder<Team> invitingTeam = TeamUtils.getTeam(sender);

//		if (TeamUtils.hasTeam(playerToInviteUUID) || invitingTeam.isEmpty()) return;

		Team team = invitingTeam.getOrThrow();

		team.addInvitee(playerToInviteUUID);
		Objects.requireNonNull(ServerEvents.server.getPlayerList().getPlayer(playerToInviteUUID)).sendSystemMessage(Component.literal(team.getName() + " has invited you to join, click here to accept")
			.setStyle(Style.EMPTY
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitolTeams inviteAccept " + team.getTeamId()))
				.withColor(TextColor.fromRgb(0x00FF00))));
	}
}