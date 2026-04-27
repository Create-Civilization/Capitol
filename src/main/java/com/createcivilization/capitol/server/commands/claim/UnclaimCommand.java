package com.createcivilization.capitol.server.commands.claim;

import com.createcivilization.capitol.common.compat.sable.SableCompat;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.UUID;

public class UnclaimCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("unclaim")
			.then(Commands.literal("chunk")
				.executes(UnclaimCommand::unclaimChunk));

		if (SableCompat.LOADED) {
			builder.then(Commands.literal("sub_level")
				.executes(UnclaimCommand::unclaimSubLevel));
		}

		return builder;
	}

	private static int unclaimChunk(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.UNCLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to unclaim chunks")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		ChunkPos chunkPos = player.chunkPosition();
		Team owner = database.getChunkOwner(chunkPos, player.level());
		if (owner == null) {
			context.getSource().sendFailure(Component.literal("This chunk is not claimed").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!owner.getId().equals(team.getId())) {
			context.getSource().sendFailure(Component.literal("Your team does not own this chunk").withStyle(ChatFormatting.RED));
			return 0;
		}

		database.unclaimChunk(team, chunkPos, player.level());
		S2CChunkRemove packet = new S2CChunkRemove(new Vector3f(chunkPos.x, 0, chunkPos.z));
		PacketDistributor.sendToPlayersTrackingChunk(context.getSource().getLevel(), chunkPos, packet);

		context.getSource().sendSuccess(() -> Component.literal("Chunk unclaimed from ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(team.getName()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}

	private static int unclaimSubLevel(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.UNCLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to unclaim")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		UUID subLevelId = SableCompat.getPlayerSubLevelId(player);
		if (subLevelId == null) {
			context.getSource().sendFailure(Component.literal("You are not in any sub-level").withStyle(ChatFormatting.RED));
			return 0;
		}

		Team owner = database.getSubLevelOwner(subLevelId);
		if (owner == null) {
			context.getSource().sendFailure(Component.literal("This sub-level is not claimed").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!owner.getId().equals(team.getId())) {
			context.getSource().sendFailure(Component.literal("Your team does not own this sub-level").withStyle(ChatFormatting.RED));
			return 0;
		}

		database.removeSubLevel(subLevelId);
		context.getSource().sendSuccess(() -> Component.literal("Sub-level unclaimed from ").withStyle(ChatFormatting.GRAY)
			.append(Component.literal(team.getName()).withStyle(ChatFormatting.GOLD)), true);
		return 1;
	}
}
