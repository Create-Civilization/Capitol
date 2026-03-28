package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.common.data.ClaimedChunk;
import com.createcivilization.capitol.common.data.Role;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class TeamCommand {

	//TODO REMOVE THIS AS THIS IS TEMPORARY

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("team")
			.then(Commands.literal("create")
				.then(Commands.argument("name", StringArgumentType.string())
					.then(Commands.argument("color", StringArgumentType.word())
						.executes(TeamCommand::createTeam))))
			.then(Commands.literal("delete")
				.then(Commands.argument("uuid", StringArgumentType.string())
					.executes(TeamCommand::deleteTeam)));
	}

	private static int createTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CapitolDatabase database = DatabaseManager.database;
		String name = StringArgumentType.getString(context, "name");
		String hex = StringArgumentType.getString(context, "color").replace("#", "");
		Color color;
		try {
			color = new Color((int) Long.parseLong(hex, 16), true);
		} catch (NumberFormatException e) {
			context.getSource().sendFailure(Component.literal("Invalid hex color: #" + hex));
			return 0;
		}
		Team team = Team.builder().name(name).id(UUID.randomUUID()).color(color).build();
		database.addTeam(team);
		database.addPlayerToTeam(context.getSource().getPlayer(), team, Role.OWNER);
		context.getSource().getPlayer().sendSystemMessage(Component.literal("Team created!").withStyle(ChatFormatting.GREEN));
		return 1;
	}

	private static int deleteTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CapitolDatabase database = DatabaseManager.database;
		UUID uuid;
		try {
			uuid = UUID.fromString(StringArgumentType.getString(context, "uuid"));
		} catch (IllegalArgumentException e) {
			context.getSource().sendFailure(Component.literal("Invalid UUID."));
			return 0;
		}

		Team team = database.getTeam(uuid);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("Team not found."));
			return 0;
		}

		List<ClaimedChunk> chunks = database.getTeamChunks(team);
		database.unclaimAllChunks(team);
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

}
