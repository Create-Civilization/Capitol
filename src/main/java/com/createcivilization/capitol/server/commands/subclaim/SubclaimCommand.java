package com.createcivilization.capitol.server.commands.subclaim;

import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.SubClaim;
import com.createcivilization.capitol.common.data.SubClaimProtection;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CSubClaimRemove;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.UUID;

public class SubclaimCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("subclaim")
			.then(Commands.literal("list")
				.executes(SubclaimCommand::list))
			.then(Commands.literal("permission")
				.then(Commands.argument("id", StringArgumentType.word())
					.then(Commands.argument("permission_value", StringArgumentType.word())
						.suggests((context, builder) -> {
							for (Permission permission : Permission.values()) {
								builder.suggest(permission.name().toLowerCase());
							}
							return builder.buildFuture();
						})
						.executes(SubclaimCommand::togglePermission))))
			.then(Commands.literal("protection")
				.then(Commands.argument("id", StringArgumentType.word())
					.then(Commands.argument("protection_value", StringArgumentType.word())
						.suggests((context, builder) -> {
							for (SubClaimProtection protection : SubClaimProtection.values()) {
								builder.suggest(protection.getKey());
							}
							return builder.buildFuture();
						})
						.executes(SubclaimCommand::toggleProtection))))
			.then(Commands.literal("delete")
				.then(Commands.argument("id", StringArgumentType.word())
					.executes(SubclaimCommand::delete)));
	}

	private static int list(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		List<SubClaim> subClaims = database.getSubClaimsOwnedBy(player.getUUID());
		if (subClaims.isEmpty()) {
			context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.subclaim.list.empty").withStyle(ChatFormatting.YELLOW), false);
			return 1;
		}

		for (SubClaim subClaim : subClaims) {
			context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.subclaim.list.entry",
				Component.literal(subClaim.name()).withStyle(ChatFormatting.WHITE),
				Component.literal(subClaim.id().toString()).withStyle(ChatFormatting.GRAY))
				.withStyle(ChatFormatting.GRAY), false);
		}
		return 1;
	}

	private static int togglePermission(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		SubClaim subClaim = getOwnedSubClaim(context, database);
		if (subClaim == null) return 0;

		String permissionName = StringArgumentType.getString(context, "permission_value").toUpperCase();
		Permission permission;
		try {
			permission = Permission.valueOf(permissionName);
		} catch (IllegalArgumentException e) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.invalid_permission", permissionName).withStyle(ChatFormatting.RED));
			return 0;
		}

		boolean oldState = subClaim.hasPermission(permission);
		boolean newState = database.toggleSubClaimPermission(subClaim.id(), permission);

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.subclaim.permission.toggled",
			Component.literal(permission.name().toLowerCase()).withStyle(ChatFormatting.AQUA),
			Component.literal(subClaim.name()).withStyle(ChatFormatting.WHITE),
			Component.translatable(oldState ? "commands.capitol.enabled" : "commands.capitol.disabled")
				.withStyle(oldState ? ChatFormatting.GREEN : ChatFormatting.RED),
			Component.translatable(newState ? "commands.capitol.enabled" : "commands.capitol.disabled")
				.withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED))
			.withStyle(ChatFormatting.GRAY), true);
		return 1;
	}

	private static int toggleProtection(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		SubClaim subClaim = getOwnedSubClaim(context, database);
		if (subClaim == null) return 0;

		String protectionName = StringArgumentType.getString(context, "protection_value").toUpperCase();
		SubClaimProtection protection;
		try {
			protection = SubClaimProtection.valueOf(protectionName);
		} catch (IllegalArgumentException e) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.invalid_protection", protectionName).withStyle(ChatFormatting.RED));
			return 0;
		}

		boolean newState = database.toggleSubClaimProtection(subClaim.id(), protection);

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.subclaim.protection.toggled",
			Component.literal(protection.getKey()).withStyle(ChatFormatting.AQUA),
			Component.literal(subClaim.name()).withStyle(ChatFormatting.WHITE),
			Component.translatable(newState ? "commands.capitol.enabled" : "commands.capitol.disabled")
				.withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED))
			.withStyle(ChatFormatting.GRAY), true);
		return 1;
	}

	private static int delete(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		SubClaim subClaim = getSubClaim(context, database);
		if (subClaim == null) return 0;

		boolean isOwner = database.isSubClaimOwner(subClaim.id(), player.getUUID());
		Team team = database.getPlayerTeam(player);
		boolean canManageTeam = team != null && Permission.MANAGE_TEAM.hasPermission(database.getPlayerPermission(player, team));

		if (!isOwner && !canManageTeam) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.subclaim.not_owner").withStyle(ChatFormatting.RED));
			return 0;
		}

		database.removeSubClaim(subClaim.id());
		PacketDistributor.sendToAllPlayers(new S2CSubClaimRemove(subClaim.id()));

		context.getSource().sendSuccess(() -> Component.translatable("commands.capitol.subclaim.deleted",
			Component.literal(subClaim.name()).withStyle(ChatFormatting.WHITE))
			.withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	/** Parses the {@code id} argument and returns a sub-claim owned by the sender, or null after sending a failure message. */
	private static SubClaim getOwnedSubClaim(CommandContext<CommandSourceStack> context, CapitolDatabase database) {
		SubClaim subClaim = getSubClaim(context, database);
		if (subClaim == null) return null;

		Player player = context.getSource().getPlayer();
		if (!database.isSubClaimOwner(subClaim.id(), player.getUUID())) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.subclaim.not_owner").withStyle(ChatFormatting.RED));
			return null;
		}
		return subClaim;
	}

	/** Parses the {@code id} argument and returns the matching sub-claim, or null after sending a failure message. */
	private static SubClaim getSubClaim(CommandContext<CommandSourceStack> context, CapitolDatabase database) {
		String idString = StringArgumentType.getString(context, "id");
		UUID id;
		try {
			id = UUID.fromString(idString);
		} catch (IllegalArgumentException e) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.subclaim.not_found").withStyle(ChatFormatting.RED));
			return null;
		}

		SubClaim subClaim = database.getSubClaim(id);
		if (subClaim == null) {
			context.getSource().sendFailure(Component.translatable("commands.capitol.subclaim.not_found").withStyle(ChatFormatting.RED));
			return null;
		}
		return subClaim;
	}
}