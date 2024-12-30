package com.createcivilization.capitol.command.custom.teamcommands.team;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import com.mojang.brigadier.arguments.StringArgumentType;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.Text;

import wiiu.mavity.util.ObjectHolder;

import java.util.*;

public class InviteAcceptTeamCommand extends AbstractTeamCommand {
	public InviteAcceptTeamCommand() {
		super("inviteAccept");
		command = CommandManager.literal(subCommandName).requires(this::canExecuteAllParams).then(
				CommandManager.argument("teamId", StringArgumentType.string()).executes(this::executeAllParams)
		);
	}

	@Override
	public int executeAllParams(CommandContext<ServerCommandSource> context) {
		ObjectHolder<Team> invitingTeamHolder = TeamUtils.getTeam(StringArgumentType.getString(context, "teamId"));
		if (invitingTeamHolder.isEmpty()) return -1;
		Team invitingTeam = invitingTeamHolder.getOrThrow();
		ServerCommandSource source = context.getSource();
		PlayerEntity player = Objects.requireNonNull(source.getPlayer());
		UUID uuid = player.getUuid();
		if (TeamUtils.hasTeam(player)){
			source.sendError(Text.literal("You're already in a team"));
			return -1;
		}
		if (
			invitingTeam.hasInvitee(uuid)
			&& (invitingTeam.getInviteeTimestamp(uuid) + Config.inviteTimeout.getOrThrow()) > (System.currentTimeMillis() / 1000L)
		){
			invitingTeam.addPlayer("member", uuid);
			source.sendFeedback(() -> Text.literal("Successfully joined team \"" + invitingTeam.getName() + "\""), true);
			return 1;
		}else{
			source.sendError(Text.literal("Invite Expired!"));
			return -1;
		}
	}

	@Override
	public boolean canExecute(PlayerEntity player) {
		setMustWhat("be a player and be in a team");
		return TeamUtils.hasTeam(player);
	}
}
