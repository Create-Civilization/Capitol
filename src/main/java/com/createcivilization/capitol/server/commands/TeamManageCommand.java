package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.common.data.*;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

class TeamManageCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("manage")
			.then(TeamRoleCommand.register())
			.then(Commands.literal("disband")
				.executes(TeamManageCommand::promptDeleteTeam))
			.then(Commands.literal("kick")
				.then(Commands.argument("player", StringArgumentType.string())
					.suggests((context, builder) -> {
						CapitolDatabase database = DatabaseManager.database;
						Team team = database.getPlayerTeam(context.getSource().getPlayer());
						if (team == null) {
							builder.suggest("YOU ARE NOT IN A TEAM");
							return builder.buildFuture();
						}
						List<TeamMember> members = database.getTeamMembers(team);
						GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
						for (TeamMember member : members) {
							profileCache.get(member.playerUUID()).ifPresent(
								gameProfile -> builder.suggest(gameProfile.getName())
							);
						}
						return builder.buildFuture();
					})
					.executes(TeamManageCommand::kickPlayer)))
			.then(Commands.literal("confirm_disband")
				.executes(TeamManageCommand::confirmDeleteTeam));
	}

	private static int promptDeleteTeam(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_TEAM.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to delete this team")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		context.getSource().sendSuccess(() ->
			Component.literal("Are you sure you would like to delete ")
				.withStyle(ChatFormatting.RED)
				.append(Component.literal(team.getName())
					.withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED))
				.append(Component.literal("? "))
				.append(Component.literal("[YES]")
					.withStyle(style -> style
						.withColor(ChatFormatting.GREEN)
						.withBold(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitol team confirm_disband"))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to confirm deletion")))))
				.append(Component.literal(" "))
				.append(Component.literal("[NO]")
					.withStyle(style -> style
						.withColor(ChatFormatting.DARK_RED)
						.withBold(true))), false);
		return 1;
	}

	private static int confirmDeleteTeam(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_TEAM.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to delete this team")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		List<ClaimedChunk> chunks = database.getTeamChunks(team);
		database.removeTeam(team);

		for (ClaimedChunk chunk : chunks) {
			ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(chunk.dimension()));
			ServerLevel level = context.getSource().getServer().getLevel(dimKey);
			if (level == null) continue;
			ChunkPos chunkPos = new ChunkPos(chunk.chunkX(), chunk.chunkZ());
			S2CChunkRemove packet = new S2CChunkRemove(new Vector3f(chunkPos.x, 0, chunkPos.z));
			PacketDistributor.sendToPlayersTrackingChunk(level, chunkPos, packet);
		}

		context.getSource().sendSuccess(() -> Component.literal("Team deleted.").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int kickPlayer(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		var player = context.getSource().getPlayer();
		Team team = database.getPlayerTeam(player);
		String playerName = StringArgumentType.getString(context, "player");
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.KICK_MEMBERS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to kick players from this team")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
		Optional<GameProfile> profile = profileCache.get(playerName);
		if (profile.isEmpty()) {
			context.getSource().sendFailure(Component.literal("There is no player called " + profile.get().getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfile gameProfile = profile.get();

		database.removePlayerFromTeam(gameProfile.getId(), team);

		context.getSource().sendSuccess(() -> Component.literal("Kicked " + gameProfile.getName() + " from the team")
			.withStyle(ChatFormatting.GREEN), true);

		return 1;
	}
}