package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.FileUtils;
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

import java.io.IOException;
import java.util.List;

import static com.createcivilization.capitol.util.TeamUtils.parseTeams;

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
		Player toInvite;
		try {
			toInvite = EntityArgument.getPlayer(context, "player");
		} catch (CommandSyntaxException e) {
			throw new RuntimeException(e);
		}
		assert invitingTeam != null;
		invitingTeam.addInvitee(toInvite.getUUID());
		toInvite.sendSystemMessage(Component.literal(invitingTeam.getName() + " has invited you to join, click here to accept")
			.setStyle(Style.EMPTY
			.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitolTeams inviteAccept " + invitingTeam.getTeamId()))
			.withColor(TextColor.fromRgb(0x00FF00))));

		context.getSource().sendSuccess(() -> Component.literal("Successfully invited player to team"), true);
		return 1;
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and be in a team");
		return TeamUtils.hasTeam(player);
	}
}
