package com.createcivilization.capitol.server.commands.war;

import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.War;
import com.createcivilization.capitol.common.events.WarEvent;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.server.networking.ServerPayloadHandler;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;

public class WarCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("war")
			.then(Commands.literal("declare")
				.then(Commands.argument("team", StringArgumentType.string())
					.suggests((context, builder) -> {
						CapitolDatabase database = DatabaseManager.database;
						Player player = context.getSource().getPlayer();
						Team team = player == null ? null : database.getPlayerTeam(player);
						for (String name : database.getAllTeamNames()) {
							if (team != null && name.equals(team.getName())) continue;
							builder.suggest(name);
						}
						return builder.buildFuture();
					})
					.executes(WarCommand::declareWar)))
			.then(Commands.literal("end")
				.then(Commands.argument("team", StringArgumentType.string())
					.suggests((context, builder) -> {
						CapitolDatabase database = DatabaseManager.database;
						Player player = context.getSource().getPlayer();
						Team team = player == null ? null : database.getPlayerTeam(player);
						if (team == null) return builder.buildFuture();
						for (War war : database.getWarsForTeam(team)) {
							if (war.isDeclarer(team.getId())) builder.suggest(war.receivingTeamName());
						}
						return builder.buildFuture();
					})
					.executes(WarCommand::endWar)))
			.then(Commands.literal("list").executes(WarCommand::listWars))
			.then(Commands.literal("debug")
				.requires(source -> source.hasPermission(4))
				.then(Commands.literal("startWar")
					.then(Commands.argument("attacker", StringArgumentType.string())
						.suggests((context, builder) -> {
							DatabaseManager.database.getAllTeamNames().forEach(builder::suggest);
							return builder.buildFuture();
						})
						.then(Commands.argument("defender", StringArgumentType.string())
							.suggests((context, builder) -> {
								DatabaseManager.database.getAllTeamNames().forEach(builder::suggest);
								return builder.buildFuture();
							})
							.executes(WarCommand::debugStartWar)))));
	}

	private static int declareWar(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team declaring = database.getPlayerTeam(player);
		if (declaring == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.DECLARE_WAR.hasPermission(database.getPlayerPermission(player, declaring))) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.declare.no_permission").withStyle(ChatFormatting.RED));
			return 0;
		}

		Team receiving = database.getTeamByName(StringArgumentType.getString(context, "team"));
		if (receiving == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.declare.invalid_team", StringArgumentType.getString(context, "team")).withStyle(ChatFormatting.RED));
			return 0;
		}

		if (receiving.getId().equals(declaring.getId())) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.declare.self").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (database.warExists(declaring.getId(), receiving.getId())) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.declare.already_at_war", receiving.getName()).withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!database.addWar(declaring, receiving)) return 0;

		War war = database.getWar(declaring.getId(), receiving.getId());
		if (war != null) NeoForge.EVENT_BUS.post(new WarEvent.WarCreatedEvent(war));
		ServerPayloadHandler.broadcastWars();

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.war.declare.success", receiving.getName()).withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int endWar(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team declaring = database.getPlayerTeam(player);
		if (declaring == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.DECLARE_WAR.hasPermission(database.getPlayerPermission(player, declaring))) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.declare.no_permission").withStyle(ChatFormatting.RED));
			return 0;
		}

		Team receiving = database.getTeamByName(StringArgumentType.getString(context, "team"));
		if (receiving == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.declare.invalid_team", StringArgumentType.getString(context, "team")).withStyle(ChatFormatting.RED));
			return 0;
		}

		War war = database.getWar(declaring.getId(), receiving.getId());
		if (war == null || !war.isDeclarer(declaring.getId())) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.end.not_at_war", receiving.getName()).withStyle(ChatFormatting.RED));
			return 0;
		}

		database.removeWar(war.declaringTeamId(), war.receivingTeamId());
		com.createcivilization.capitol.server.events.WarTakeoverEvents.clearWarState(war);
		ServerPayloadHandler.broadcastWars();

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.war.end.success", receiving.getName()).withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int listWars(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED));
			return 0;
		}

		List<War> wars = database.getWarsForTeam(team);
		if (wars.isEmpty()) {
			context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.war.list.empty").withStyle(ChatFormatting.GRAY), false);
			return 1;
		}

		MutableComponent msg = Component.translatable("commands.capitol.war.list.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
		for (War war : wars) {
			String side = war.isDeclarer(team.getId()) ? "ATTACKING" : "DEFENDING";
			msg.append(Component.literal("\n"))
				.append(Component.literal(war.declaringTeamName() + " vs " + war.receivingTeamName()).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(" [" + side + "]").withStyle(ChatFormatting.GRAY));
		}
		context.getSource().sendSuccess(() -> msg, false);
		return 1;
	}

	// migrated from ActivateWarTeamsDebugCommand: force-starts a war between two teams (op only)
	private static int debugStartWar(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		String attackerName = StringArgumentType.getString(context, "attacker");
		String defenderName = StringArgumentType.getString(context, "defender");

		Team attacker = database.getTeamByName(attackerName);
		Team defender = database.getTeamByName(defenderName);
		if (attacker == null || defender == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.debug.invalid_team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (attacker.getId().equals(defender.getId()) || database.warExists(attacker.getId(), defender.getId())) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.war.declare.already_at_war", defender.getName()).withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!database.addWar(attacker, defender)) return 0;

		War war = database.getWar(attacker.getId(), defender.getId());
		if (war != null) NeoForge.EVENT_BUS.post(new WarEvent.WarCreatedEvent(war));
		ServerPayloadHandler.broadcastWars();

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.war.debug.success", attackerName, defenderName).withStyle(ChatFormatting.GREEN), true);
		return 1;
	}
}