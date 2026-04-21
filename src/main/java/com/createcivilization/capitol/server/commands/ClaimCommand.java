package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.common.compat.sable.Sable;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

public class ClaimCommand {

	static LiteralArgumentBuilder<CommandSourceStack> register() {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("claim")
			.then(Commands.literal("chunk")
				.executes(ClaimCommand::claim))
			.then(Commands.literal("info")
				.executes(ClaimCommand::info));

		if (ModList.get().isLoaded("sable")) {
			builder.then(Commands.literal("sub_level")
				.executes(ClaimCommand::claimSubLevel));
		}

		return builder;
	}
	private static int claim(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to claim chunks")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		ChunkPos chunkPos = player.chunkPosition();
		Team existingOwner = database.getChunkOwner(chunkPos, player.level());
		if (existingOwner != null) {
			context.getSource().sendFailure(Component.literal("This chunk is already claimed by " + existingOwner.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		database.claimChunk(team, chunkPos, player.level());
		S2CChunkData packet = new S2CChunkData(new Vector3f(chunkPos.x, 0, chunkPos.z), team);
		PacketDistributor.sendToPlayersTrackingChunk(context.getSource().getLevel(), chunkPos, packet);

		context.getSource().sendSuccess(() -> Component.literal("Chunk claimed!").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int claimSubLevel(CommandContext<CommandSourceStack> context){
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			context.getSource().sendFailure(Component.literal("You are not in any team").withStyle(ChatFormatting.RED));
			return 0;
		}

		if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			context.getSource().sendFailure(Component.literal("You do not have permission to claim chunks")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		SubLevelAccess subLevelAccess = null;

		//So I kinda need to do this as sable companion does not have a way to get a tracked sublevel *yet*
		if(ModList.get().isLoaded("sable")){
			subLevelAccess = Sable.getPlayerSublevel(player);
		}

		if(subLevelAccess == null){
			context.getSource().sendFailure(Component.literal("You are not in any sub-level")
				.withStyle(ChatFormatting.RED));
			return 0;
		}

		Team existingOwner = database.getSubLevelOwner(subLevelAccess.getUniqueId());
		if (existingOwner != null) {
			context.getSource().sendFailure(Component.literal("This sub-level is already claimed by " + existingOwner.getName())
				.withStyle(ChatFormatting.RED));
			return 0;
		}


		database.claimSubLevel(subLevelAccess.getUniqueId(), team);
		context.getSource().sendSuccess(() -> Component.literal("Sub-level claimed!").withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int info(CommandContext<CommandSourceStack> context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.getSource().getPlayer();
		if (player == null) return 0;

		Team chunkOwner = database.getChunkOwner(player.chunkPosition(), player.level());
		if (chunkOwner == null) {
			context.getSource().sendSuccess(() -> Component.literal("This chunk is not claimed").withStyle(ChatFormatting.YELLOW), false);
			return 1;
		}

		context.getSource().sendSuccess(() -> Component.literal("Claimed by: " + chunkOwner.getName()).withStyle(ChatFormatting.GREEN), false);
		return 1;
	}

}
