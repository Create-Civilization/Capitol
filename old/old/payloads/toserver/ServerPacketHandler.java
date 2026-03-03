package com.createcivilization.capitol.old.old.payloads.toserver;

import com.createcivilization.capitol.old.old.constants.ServerConstants;
import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.old.payloads.bidirectional.add.BiAddWar;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.team.War;

import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.old.util.team.LogToDiscord;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import net.neoforged.api.distmarker.*;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

import java.awt.Color;
import java.util.*;

@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerPacketHandler {

	public static void syncDataWithPlayer(ServerPlayer sender) {
		DataManager.synchronizeServerDataWithPlayer(sender);
	}

    public static void createTeam(String teamName, ServerPlayer sender, Color teamColor) {
		if (TeamUtils.hasTeam(sender)) return;
		DataManager.TeamData.LOADED_OLD_TEAMS.add(TeamUtils.createTeam(teamName, sender, teamColor));
    }

	public static void claimCurrentPlayerChunk(ServerPlayer sender) {
		if (TeamUtils.isInClaimedChunk(sender) || !TeamUtils.chunkIsNearChildChunk(sender.chunkPosition(), 1, sender)) return;
		TeamUtils.claimCurrentChunk(sender);
	}

	public static void claimChunk(ResourceLocation dimension, ChunkPos pos, OldTeam oldTeam) {
		if (TeamUtils.isChunkParent(oldTeam, dimension, pos) || !TeamUtils.chunkIsNearChildChunk(pos, 1, dimension, oldTeam)) return;

		TeamUtils.claimChunk(oldTeam, dimension, pos);
	}

	public static void invitePlayerToTeam(ServerPlayer sender, String playerToInviteName) {
		PlayerList playerList = ServerConstants.server.getOrThrow().getPlayerList();
		ObjectHolder<OldTeam> invitingTeam = TeamUtils.getTeam(sender);
		Player player = playerList.getPlayerByName(playerToInviteName);
		if (player == null) return;
		UUID playerToInviteUUID = Objects.requireNonNull(playerList.getPlayerByName(playerToInviteName)).getUUID();

		if (TeamUtils.hasTeam(playerToInviteUUID) || invitingTeam.isEmpty()) return;

		OldTeam oldTeam = invitingTeam.getOrThrow();
		if (TeamUtils.canPlayerDo(oldTeam, sender, "invitePlayers")) {
			sender.sendSystemMessage(Component.literal("You do not have the permissions to invite players!"));
			return;
		}

		oldTeam.addInvitee(playerToInviteUUID);
		Objects.requireNonNull(playerList.getPlayer(playerToInviteUUID))
			.sendSystemMessage(Component.literal(oldTeam.getName() + " has invited you to join, click here to accept")
				.setStyle(Style.EMPTY
					.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitolTeams inviteAccept " + oldTeam.getTeamId()))
					.withColor(TextColor.fromRgb(0x00FF00))
				)
			);
	}

	public static void sendTeamMessage(ServerPlayer sender, String message) {
		ObjectHolder<OldTeam> holder = TeamUtils.getTeam(sender);
		if (holder.isEmpty()) return;
		OldTeam oldTeam = holder.getOrThrow();

		var playerList = ServerConstants.server.getOrThrow().getPlayerList();
		for (UUID member : oldTeam.getAllPlayers()) {
			Player receiver = playerList.getPlayer(member);
			String msg = "[" + oldTeam.getName() + "] <" + sender.getName().getString() + "> " + message;
			if (receiver != null) receiver.displayClientMessage(Component.literal(msg), false);
			System.out.println(msg);
			LogToDiscord.postIfAllowed(oldTeam, msg);
		}
	}

	public static void unclaimChunk(ResourceLocation dimension, ChunkPos pos, OldTeam oldTeam) {
		if (!TeamUtils.isChunkParent(oldTeam, dimension, pos)) return;

		TeamUtils.unclaimChunkAndUpdate(oldTeam, dimension, pos);
	}

	public static void addWar(OldTeam declaring, OldTeam receiving) {
		War warToAdd = new War(declaring, receiving);

		if(DataManager.WarData.loadedWars.contains(warToAdd) || (warToAdd.getDeclaringTeam().equals(warToAdd.getReceivingTeam()))) return;
		DataManager.WarData.loadedWars.add(warToAdd);
		PacketHandler.sendToAllPlayers(new BiAddWar(warToAdd.getDeclaringTeam(), warToAdd.getReceivingTeam()));
	}

	public static void removeWar(War war, Player player) {
		TeamUtils.endWar(war, player);
	}
}