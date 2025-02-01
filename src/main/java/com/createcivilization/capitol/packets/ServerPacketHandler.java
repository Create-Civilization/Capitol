package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.constants.ServerConstants;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import net.minecraftforge.api.distmarker.*;
import net.minecraftforge.network.NetworkEvent;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

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
		if (TeamUtils.isInClaimedChunk(sender) || !TeamUtils.chunkIsNearChildChunk(sender.chunkPosition(), 1, sender)) return;
		TeamUtils.claimCurrentChunk(sender);
	}

	public static void claimChunk(ResourceLocation dimension, ChunkPos pos, Team team) {
		if (TeamUtils.isChunkParent(team, dimension, pos) || !TeamUtils.chunkIsNearChildChunk(pos, 1, dimension, team)) return;

		TeamUtils.claimChunk(team, dimension, pos);
	}

	public static void invitePlayerToTeam(ServerPlayer sender, String playerToInviteName) {
		var playerList = ServerConstants.server.getOrThrow().getPlayerList();
		ObjectHolder<Team> invitingTeam = TeamUtils.getTeam(sender);
		Player player = playerList.getPlayerByName(playerToInviteName); // already ignores case
		if (player == null) return;
		UUID playerToInviteUUID = Objects.requireNonNull(playerList.getPlayerByName(playerToInviteName)).getUUID();

//		if (TeamUtils.hasTeam(playerToInviteUUID) || invitingTeam.isEmpty()) return;

		Team team = invitingTeam.getOrThrow();

		team.addInvitee(playerToInviteUUID);
		Objects.requireNonNull(playerList.getPlayer(playerToInviteUUID))
			.sendSystemMessage(Component.literal(team.getName() + " has invited you to join, click here to accept")
				.setStyle(Style.EMPTY
					.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitolTeams inviteAccept " + team.getTeamId()))
					.withColor(TextColor.fromRgb(0x00FF00))
				)
			);
	}


	public static void handlePacket(Runnable run, NetworkEvent.Context ctx) {
		ctx.enqueueWork(
			() -> DistHelper.runWhenOnServer(
				() -> run
			)
		);
		ctx.setPacketHandled(true);
	}

	public static void sendTeamMessage(ServerPlayer sender, String message) {
		ObjectHolder<Team> holder = TeamUtils.getTeam(sender);
		if (holder.isEmpty()) return;
		Team team = holder.getOrThrow();

		var playerList = ServerConstants.server.getOrThrow().getPlayerList();
		for (UUID member : team.getAllPlayers()) {
			Player receiver = playerList.getPlayer(member);
			String msg = "[" + team.getName() + "] <" + sender.getName().getString() + "> " + message;
			if (receiver != null) receiver.displayClientMessage(Component.literal(msg), false);
			System.out.println(msg);
			LogToDiscord.postIfAllowed(team, msg);
		}
	}

	public static void unclaimChunk(ResourceLocation dimension, ChunkPos pos, Team team) {
		if (!TeamUtils.isChunkParent(team, dimension, pos)) return;

		TeamUtils.unclaimChunkAndUpdate(team, dimension, pos);
	}
}