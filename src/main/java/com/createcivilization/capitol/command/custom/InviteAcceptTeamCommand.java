package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.Config;
import com.createcivilization.capitol.util.TeamUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import wiiu.mavity.util.ObjectHolder;

import java.util.Objects;
import java.util.UUID;

public class InviteAcceptTeamCommand extends AbstractTeamCommand {
	public InviteAcceptTeamCommand() {
		super("inviteAccept");
		command = Commands.literal(commandName).requires(this::canExecuteAllParams).then(
				Commands.argument("teamId", StringArgumentType.string()).executes(this::executeAllParams)
		);
	}

	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> context) {
		ObjectHolder<Team> invitingTeamHolder = TeamUtils.getTeam(StringArgumentType.getString(context, "teamId"));
		if (invitingTeamHolder.isEmpty()) return -1;
		Team invitingTeam = invitingTeamHolder.getOrThrow();
		CommandSourceStack source = context.getSource();
		Player player = Objects.requireNonNull(source.getPlayer());
		UUID uuid = player.getUUID();
		if (TeamUtils.hasTeam(player)){
			source.sendFailure(Component.literal("You're already in a team"));
			return -1;
		}
		if (
			invitingTeam.hasInvitee(uuid)
			&& (invitingTeam.getInviteeTimestamp(uuid) + Config.inviteTimeout.getOrThrow()) > (System.currentTimeMillis() / 1000L)
		){
			invitingTeam.addPlayer("member", uuid);
			source.sendSuccess(() -> Component.literal("Successfully joined team \"" + invitingTeam.getName() + "\""), true);
			return 1;
		}else{
			source.sendFailure(Component.literal("Invite Expired!"));
			return -1;
		}
	}

	@Override
	public boolean canExecute(Player player) {
		setMustWhat("be a player and be in a team");
		return TeamUtils.hasTeam(player);
	}
}
