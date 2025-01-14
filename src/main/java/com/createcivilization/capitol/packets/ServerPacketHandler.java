package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.constants.ServerConstants;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
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

	public static void claimChunk(ServerPlayer sender, BlockPos pos) {
		if (TeamUtils.isInClaimedChunk(sender, pos) || !TeamUtils.nearClaimedChunk(new ChunkPos(pos), 1, sender)) return;

		TeamUtils.claimChunk(sender, pos);
	}

	public static void invitePlayerToTeam(ServerPlayer sender, UUID playerToInviteUUID) {
		ObjectHolder<Team> invitingTeam = TeamUtils.getTeam(sender);

		if (TeamUtils.hasTeam(playerToInviteUUID) || invitingTeam.isEmpty()) return;

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
			if (toSend != null) toSend.displayClientMessage(Component.literal("[" + team.getName() + "] <").append(sender.getDisplayName()).append("> " + message), false);
			System.out.println("[" + team.getName() + "] <" + sender.getName() + "> " + message);
		}
	}
}