package com.createcivilization.capitol.server.commands.team;

import com.createcivilization.capitol.common.data.*;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.server.invites.InviteHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TeamCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("team")
			.then(Commands.literal("create")
				.then(Commands.argument("name", StringArgumentType.string())
					.then(Commands.argument("tag", StringArgumentType.word())
						.then(Commands.argument("color", StringArgumentType.word())
							.then(Commands.argument("description", StringArgumentType.string())
								.executes(TeamCommand::createTeam))
							.executes(TeamCommand::createTeam)))))
			.then(Commands.literal("info").executes(TeamCommand::teamInfo))
			.then(Commands.literal("invite")
				.then(Commands.argument("player", EntityArgument.player())
					.executes(TeamCommand::invitePlayer)))
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
					.executes(TeamCommand::kickPlayer)))
			.then(Commands.literal("disband").executes(TeamCommand::promptDisband))
			.then(Commands.literal("confirm_disband").executes(TeamCommand::confirmDisband))
			.then(TeamRoleCommand.register())
			.then(TeamProtectionCommand.register());
	}

	private static int invitePlayer(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		ServerPlayer playerToInvite;
		try {
			playerToInvite = EntityArgument.getPlayer(context, "player");
		} catch (CommandSyntaxException e) {
			context.getSource().sendFailure(Component.literal("Invalid Player").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (database.getPlayerTeam(playerToInvite) != null) {
			context.getSource().sendFailure(Component.literal("Player is already in a team").withStyle(ChatFormatting.RED));
			return 0;
		}

		InviteHandler.addInvite(playerToInvite, team);

		String acceptCmd = "/capitol invites " + team.getName() + " accept";
		String denyCmd   = "/capitol invites " + team.getName() + " deny";

		MutableComponent accept = Component.literal("[Accept]")
			.withStyle(s -> s.withColor(ChatFormatting.GREEN)
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, acceptCmd))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(acceptCmd))));

		MutableComponent deny = Component.literal("[Deny]")
			.withStyle(s -> s.withColor(ChatFormatting.RED)
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, denyCmd))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(denyCmd))));

		MutableComponent msg = Component.literal("You have been invited to join ")
			.withStyle(ChatFormatting.YELLOW)
			.append(Component.literal(team.getName()).withStyle(ChatFormatting.GOLD))
			.append(Component.literal("! ").withStyle(ChatFormatting.YELLOW))
			.append(accept)
			.append(Component.literal(" ").withStyle(ChatFormatting.WHITE))
			.append(deny);

		playerToInvite.sendSystemMessage(msg);

		context.getSource().sendSuccess(() -> Component.literal("Invite sent to " + playerToInvite.getName().getString() + ".").withStyle(ChatFormatting.GREEN), false);
		return 1;
	}

	private static int teamInfo(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		int rgb = team.getColor().getRGB() & 0xFFFFFF;
		String hex = String.format("#%06X", rgb);
		String claims = team.getMaxClaims() > 0
			? team.getCurrentClaims() + " / " + team.getMaxClaims()
			: String.valueOf(team.getCurrentClaims());
		String description = team.getDescription() != null && !team.getDescription().isEmpty()
			? team.getDescription()
			: "None";

		MutableComponent msg = Component.empty()
			.append(Component.literal("[" + team.getTag() + "] ").withStyle(ChatFormatting.AQUA))
			.append(Component.literal(team.getName()).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
			.append(Component.literal("\n"))
			.append(Component.literal("Color: ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal("█ ").withStyle(s -> s.withColor(rgb)))
			.append(Component.literal(hex).withStyle(ChatFormatting.WHITE))
			.append(Component.literal("\n"))
			.append(Component.literal("Description: ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(description).withStyle(ChatFormatting.WHITE))
			.append(Component.literal("\n"))
			.append(Component.literal("Claims: ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(claims).withStyle(ChatFormatting.WHITE));

		context.getSource().sendSuccess(() -> msg, false);
		return 1;
	}

	private static int createTeam(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		if (database.getPlayerTeam(player) != null) {
			context.getSource().sendFailure(Component.literal("You are already in a team, please leave before creating a new one")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		String name = StringArgumentType.getString(context, "name");

		if (database.teamNameExists(name)) {
			context.getSource().sendFailure(Component.literal("A team with that name already exists.").withStyle(ChatFormatting.RED));
			return 0;
		}

		String hex = StringArgumentType.getString(context, "color").replace("#", "");
		String tag = StringArgumentType.getString(context, "tag");

		String description;
		try {
			description = StringArgumentType.getString(context, "description");
		} catch (IllegalArgumentException e) {
			description = "";
		}

		Color color;
		try {
			color = new Color((int) Long.parseLong(hex, 16), true);
		} catch (NumberFormatException e) {
			context.getSource().sendFailure(Component.literal("Invalid hex color: #" + hex).withStyle(ChatFormatting.RED));
			return 0;
		}

		Team team = Team.builder().name(name).id(UUID.randomUUID()).color(color).description(description).tag(tag).build();
		database.addTeam(team);
		TeamRole ownerRole = database.getRoleByName(team, TeamRole.OWNER_ROLE_NAME);
		database.addPlayerToTeam(player, team, ownerRole);
		context.getSource().sendSuccess(() -> Component.literal("Team Created!").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int kickPlayer(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
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
			context.getSource().sendFailure(Component.literal("There is no player called " + playerName).withStyle(ChatFormatting.RED));
			return 0;
		}

		GameProfile gameProfile = profile.get();
		database.removePlayerFromTeam(gameProfile.getId(), team);

		context.getSource().sendSuccess(() -> Component.literal("Kicked " + gameProfile.getName() + " from the team")
			.withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int promptDisband(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_TEAM.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to disband this team")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		context.getSource().sendSuccess(() ->
			Component.literal("Are you sure you would like to delete ")
				.withStyle(ChatFormatting.RED)
				.append(Component.literal(team.getName()).withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED))
				.append(Component.literal("? "))
				.append(Component.literal("[YES]")
					.withStyle(s -> s.withColor(ChatFormatting.GREEN).withBold(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/capitol team confirm_disband"))
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to confirm deletion")))))
				.append(Component.literal(" "))
				.append(Component.literal("[NO]").withStyle(s -> s.withColor(ChatFormatting.DARK_RED).withBold(true))),
			false);
		return 1;
	}

	private static int confirmDisband(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.MANAGE_TEAM.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to disband this team")
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

		context.getSource().sendSuccess(() -> Component.literal("Team disbanded.").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}
}
