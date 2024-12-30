package com.createcivilization.capitol.command.custom.teamcommands.chunks;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

public class UnclaimCurrentChunkCommand extends AbstractTeamCommand {

    public UnclaimCurrentChunkCommand() {
        super("unclaimCurrentChunk");
        command = Commands.literal(subCommandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
    }

	@Override
	@SuppressWarnings("resource")
	public int execute(Player player) {
		ChunkPos chunkPos = player.chunkPosition();
		if (!TeamUtils.isInClaimedChunk(player)) {
			// Chunk already claimed
			player.sendSystemMessage(Component.literal("Chunk not claim claimed"));
			return -1;
		}
		ResourceLocation dimension = player.level().dimension().location();
		if (!TeamUtils.allowedInChunk(player, dimension, chunkPos)) {

			// Player is not allowed to unclaim chunk
			player.sendSystemMessage(Component.literal("You are not allowed to unclaim this chunk"));
			return -1;
		}
		if (TeamUtils.hasCapitolBlock(chunkPos, dimension)) {

			// Claim has capitol block
			player.sendSystemMessage(Component.literal("You are not allowed to unclaim a chunk with a Capital in it"));
			return -1;
		}

		player.sendSystemMessage(Component.literal("Unclaiming chunk"));
		return TeamUtils.unclaimCurrentChunk(player);
	}

	@Override
    public boolean canExecute(Player player) {
        setMustWhat("be a player and be in a team");
        return TeamUtils.hasTeam(player);
    }
}