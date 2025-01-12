package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.api.distmarker.*;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wiiu.mavity.util.ObjectHolder;

import java.awt.Color;
import java.util.*;

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

		if (TeamUtils.hasTeam(playerToInviteUUID) || invitingTeam.isEmpty()) return;

		Team team = invitingTeam.getOrThrow();

		team.addInvitee(playerToInviteUUID);
		Objects.requireNonNull(Capitol.server.getOrThrow().getPlayerList().getPlayer(playerToInviteUUID)).sendSystemMessage(Component.literal(team.getName() + " has invited you to join, click here to accept")
			.setStyle(Style.EMPTY
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitolTeams inviteAccept " + team.getTeamId()))
				.withColor(TextColor.fromRgb(0x00FF00))));
	}


	public static void handlePacket(Runnable run, NetworkEvent.Context ctx) {
		ctx.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(
				Dist.DEDICATED_SERVER,
				() -> run
			)
		);
		ctx.setPacketHandled(true);
	}
}