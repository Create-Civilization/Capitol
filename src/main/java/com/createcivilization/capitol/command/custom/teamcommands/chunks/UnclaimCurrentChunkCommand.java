package com.createcivilization.capitol.command.custom.teamcommands.chunks;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

public class UnclaimCurrentChunkCommand extends AbstractTeamCommand {

    public UnclaimCurrentChunkCommand() {
        super("unclaimCurrentChunk");
        command = CommandManager.literal(commandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
    }

	@Override
	public int execute(PlayerEntity player) {
		ChunkPos chunkPos = player.getChunkPos();
		if (!TeamUtils.isInClaimedChunk(player)) {
			// Chunk already claimed
			player.sendMessage(Text.literal("Chunk not claim claimed"));
			return -1;
		}
		Identifier dimension = player.getWorld().getRegistryKey().getValue();
		if (!TeamUtils.allowedInChunk(player, dimension, chunkPos)) {

			// Player is not allowed to unclaim chunk
			player.sendMessage(Text.literal("You are not allowed to unclaim this chunk"));
			return -1;
		}
		if (TeamUtils.hasCapitolBlock(chunkPos, dimension)) {

			// Claim has capitol block
			player.sendMessage(Text.literal("You are not allowed to unclaim a chunk with a Capital in it"));
			return -1;
		}

		player.sendMessage(Text.literal("Unclaiming chunk"));
		return TeamUtils.unclaimCurrentChunk(player);
	}

	@Override
    public boolean canExecute(PlayerEntity player) {
        setMustWhat("be a player and be in a team");
        return TeamUtils.hasTeam(player);
    }
}