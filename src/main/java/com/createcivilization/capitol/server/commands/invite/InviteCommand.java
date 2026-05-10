package com.createcivilization.capitol.server.commands.invite;

import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamMember;
import com.createcivilization.capitol.common.data.TeamRole;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.server.invites.InviteHandler;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class InviteCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("invites")
			.then(Commands.argument("team", StringArgumentType.word())
				.suggests((ctx, builder) -> {
					Player player = ctx.getSource().getPlayer();
					if (player == null) return builder.buildFuture();
					List<Team> invites = InviteHandler.getInvites(player);
					if (invites == null) return builder.buildFuture();
					return SharedSuggestionProvider.suggest(
						invites.stream().map(Team::getName),
						builder
					);
				})
				.then(Commands.literal("accept").executes(InviteCommand::acceptInvite))
				.then(Commands.literal("deny").executes(InviteCommand::denyInvite)));
	}

	private static int acceptInvite(CommandContext<CommandSourceStack> context) {
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = resolveInvitedTeam(context, player);
		if (team == null) return 0;

		TeamRole role = DatabaseManager.database.getMemberRole(team);
		DatabaseManager.database.addPlayerToTeam(player, team, role);
		InviteHandler.clearInvites(player);

		MutableComponent joinMsg = Component.translatable("commands.capitol.invites.join.announcement",
			Component.literal(player.getName().getString()).withStyle(ChatFormatting.WHITE),
			Component.literal(team.getName()).withStyle(ChatFormatting.GOLD))
			.withStyle(ChatFormatting.GRAY);

		for (TeamMember member : DatabaseManager.database.getTeamMembers(team)) {
			if (member.playerUUID().equals(player.getUUID())) continue;
			ServerPlayer online = context.getSource().getServer().getPlayerList().getPlayer(member.playerUUID());
			if (online != null) online.sendSystemMessage(joinMsg);
		}

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.invites.join.success",
			Component.literal(team.getName()).withStyle(ChatFormatting.GOLD))
			.withStyle(ChatFormatting.GRAY), false);
		return 1;
	}

	private static int denyInvite(CommandContext<CommandSourceStack> context) {
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = resolveInvitedTeam(context, player);
		if (team == null) return 0;

		InviteHandler.removeInvite(player, team);
		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.invites.deny.success",
			Component.literal(team.getName()).withStyle(ChatFormatting.GOLD))
			.withStyle(ChatFormatting.GRAY), false);
		return 1;
	}

	private static Team resolveInvitedTeam(CommandContext<CommandSourceStack> context, Player player) {
		String teamName = StringArgumentType.getString(context, "team");
		List<Team> invites = InviteHandler.getInvites(player);
		if (invites == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.invites.no_invites").withStyle(ChatFormatting.RED));
			return null;
		}
		Team team = invites.stream().filter(t -> t.getName().equals(teamName)).findFirst().orElse(null);
		if (team == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.invites.not_found", teamName).withStyle(ChatFormatting.RED));
			return null;
		}
		return team;
	}
}