package com.createcivilization.capitol.command.custom.teamcommands.team;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.*;

public class InviteTeamCommand extends AbstractTeamCommand {

	public InviteTeamCommand() {
		super("invitePlayer");
		command = CommandManager.literal(commandName).requires(this::canExecuteAllParams).then(
			CommandManager.argument("player", EntityArgumentType.players()).executes(this::executeAllParams)
		);
	}

	@Override
	public int executeAllParams(CommandContext<ServerCommandSource> context) {
		Team invitingTeam = TeamUtils.getTeam(context.getSource().getPlayer()).get();
		ServerCommandSource inviter = context.getSource();
		PlayerEntity toInvite;
		try {
			toInvite = EntityArgumentType.getPlayer(context, "player");
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
		if (TeamUtils.hasTeam(toInvite)) {
			inviter.sendError(Text.literal("Player already in a team."));
			return -1;
		}
		assert invitingTeam != null;
		invitingTeam.addInvitee(toInvite.getUuid());
		toInvite.sendMessage(Text.literal(invitingTeam.getName() + " has invited you to join, click here to accept")
			.setStyle(Style.EMPTY
			.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitolTeams inviteAccept " + invitingTeam.getTeamId()))
			.withColor(TextColor.fromRgb(0x00FF00))));

		inviter.sendFeedback(() -> Text.literal("Successfully invited player to team"), true);
		return 1;
	}

	@Override
	public boolean canExecute(PlayerEntity player) {
		setMustWhat("be a player and be in a team");
		return TeamUtils.hasTeam(player);
	}
}
