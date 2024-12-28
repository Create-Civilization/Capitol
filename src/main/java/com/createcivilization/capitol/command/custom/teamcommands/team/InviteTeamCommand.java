package com.createcivilization.capitol.command.custom.teamcommands.team;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;

public class InviteTeamCommand extends AbstractTeamCommand {
	public InviteTeamCommand() {
		super("invitePlayer");
		command = Commands.literal(commandName).requires(this::canExecuteAllParams).then(
			Commands.argument("player", EntityArgument.players()).executes(this::executeAllParams)
		);
	}

	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context) {
		Team invitingTeam = TeamUtils.getTeam(context.getSource().getPlayer()).get();
		CommandSourceStack inviter = context.getSource();
		Player toInvite;
		try {
			toInvite = EntityArgument.getPlayer(context, "player");
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
		if (TeamUtils.hasTeam(toInvite)) {
			inviter.sendFailure(Component.literal("Player already in a team."));
			return -1;
		}
		assert invitingTeam != null;
		invitingTeam.addInvitee(toInvite.getUUID());
		toInvite.sendSystemMessage(Component.literal(invitingTeam.getName() + " has invited you to join, click here to accept")
			.setStyle(Style.EMPTY
			.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitolTeams inviteAccept " + invitingTeam.getTeamId()))
			.withColor(TextColor.fromRgb(0x00FF00))));

		inviter.sendSuccess(() -> Component.literal("Successfully invited player to team"), true);
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and be in a team");
		return TeamUtils.hasTeam(player);
	}
}
