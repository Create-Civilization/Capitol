package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.constants.ServerConstants;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

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

	public static void claimChunk(ResourceLocation dimension, ChunkPos pos, Team team) {
		if (TeamUtils.allowedInChunk(team, dimension, pos) || !TeamUtils.nearClaimedChunk(pos, 1, dimension, team)) return;

		TeamUtils.claimChunk(team, dimension, pos);
	}

	public static void invitePlayerToTeam(ServerPlayer sender, String playerToInviteName) {
		ObjectHolder<Team> invitingTeam = TeamUtils.getTeam(sender);
		Player player = ServerConstants.server.getOrThrow().getPlayerList().getPlayerByName(playerToInviteName); // already ignores case
		if (player == null) return;
		UUID playerToInviteUUID = Objects.requireNonNull(ServerConstants.server.getOrThrow().getPlayerList().getPlayerByName(playerToInviteName)).getUUID();

//		if (TeamUtils.hasTeam(playerToInviteUUID) || invitingTeam.isEmpty()) return;

		Team team = invitingTeam.getOrThrow();

		team.addInvitee(playerToInviteUUID);
		Objects.requireNonNull(ServerConstants.server.getOrThrow().getPlayerList().getPlayer(playerToInviteUUID)).sendSystemMessage(Component.literal(team.getName() + " has invited you to join, click here to accept")
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

	public static void sendTeamMessage(ServerPlayer sender, String message) {
		ObjectHolder<Team> holder = TeamUtils.getTeam(sender);
		if (holder.isEmpty()) return;
		Team team = holder.getOrThrow();

		for (UUID member : team.getAllPlayers()) {
			Player toSend = ServerConstants.server.getOrThrow().getPlayerList().getPlayer(member);
			String msg = "[" + team.getName() + "] <" + sender.getName().getString() + "> " + message;
			if (toSend != null) toSend.displayClientMessage(Component.literal(msg), false);
			System.out.println(msg);
		}
	}

	public static void unclaimChunk(ResourceLocation dimension, ChunkPos pos, Team team) {
		if (!TeamUtils.allowedInChunk(team, dimension, pos)) return;

		TeamUtils.unclaimChunk(team, dimension, pos);
	}
}