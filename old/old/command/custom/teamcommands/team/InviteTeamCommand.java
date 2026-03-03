package com.createcivilization.capitol.old.old.command.custom.teamcommands.team;

import com.createcivilization.capitol.old.old.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Player;

public class InviteTeamCommand extends AbstractTeamCommand {

	public InviteTeamCommand() {
		super("invitePlayer");
		command.set(
			Commands.literal(subCommandName.getOrThrow())
				.requires(this::canExecuteAllParams)
				.then(
					Commands.argument("player", EntityArgument.players())
						.executes(this::executeAllParams)
				)
		);
	}

	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context) {
		OldTeam invitingOldTeam = TeamUtils.getTeam(context.getSource().getPlayer()).get();
		CommandSourceStack inviter = context.getSource();
		Player toInvite;
		try {
			toInvite = EntityArgument.getPlayer(context, "player");
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
		if (TeamUtils.hasTeam(toInvite)) {
			inviter.sendFailure(Component.literal("Player already in a receivingOldTeam."));
			return -1;
		}
		assert invitingOldTeam != null;
		invitingOldTeam.addInvitee(toInvite.getUUID());
		toInvite.sendSystemMessage(Component.literal(invitingOldTeam.getName() + " has invited you to join, click here to accept")
			.setStyle(Style.EMPTY
			.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitolTeams inviteAccept " + invitingOldTeam.getTeamId()))
			.withColor(TextColor.fromRgb(0x00FF00))));

		inviter.sendSuccess(() -> Component.literal("Successfully invited player to receivingOldTeam"), true);
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and be in a receivingOldTeam");
		return TeamUtils.hasTeam(player);
	}
}