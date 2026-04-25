package com.createcivilization.capitol.server.commands.invite;

import com.createcivilization.capitol.common.data.Team;
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

		TeamRole role = DatabaseManager.database.getDefaultRole(team);
		DatabaseManager.database.addPlayerToTeam(player, team, role);
		InviteHandler.clearInvites(player);

		context.getSource().sendSuccess(() -> Component.literal("You joined " + team.getName() + "!").withStyle(ChatFormatting.GREEN), false);
		return 1;
	}

	private static int denyInvite(CommandContext<CommandSourceStack> context) {
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = resolveInvitedTeam(context, player);
		if (team == null) return 0;

		InviteHandler.removeInvite(player, team);
		context.getSource().sendSuccess(() -> Component.literal("Invite from " + team.getName() + " denied.").withStyle(ChatFormatting.YELLOW), false);
		return 1;
	}

	private static Team resolveInvitedTeam(CommandContext<CommandSourceStack> context, Player player) {
		String teamName = StringArgumentType.getString(context, "team");
		List<Team> invites = InviteHandler.getInvites(player);
		if (invites == null) {
			context.getSource().sendFailure(Component.literal("You have no pending invites.").withStyle(ChatFormatting.RED));
			return null;
		}
		Team team = invites.stream().filter(t -> t.getName().equals(teamName)).findFirst().orElse(null);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("No invite from team \"" + teamName + "\".").withStyle(ChatFormatting.RED));
			return null;
		}
		return team;
	}
}