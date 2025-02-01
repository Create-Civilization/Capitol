package com.createcivilization.capitol.command.custom.teamcommands.chunks;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ClaimCurrentChunkCommand extends AbstractTeamCommand {

    public ClaimCurrentChunkCommand() {
        super("claimCurrentChunk");
        command.set(Commands.literal(subCommandName.getOrThrow()).requires(this::canExecuteAllParams).executes(this::executeAllParams));
    }

	@Override
	public int execute(Player player) {
		if (TeamUtils.isInClaimedChunk(player)) {
			// Chunk already claimed
			player.sendSystemMessage(Component.literal("Chunk already claimed"));
			return -1;
		}
		if (!TeamUtils.chunkIsNearChildChunk(player.chunkPosition(), 1, player)) {
			// Near chunk to extend
			player.sendSystemMessage(Component.literal("Must be near a claimed chunk"));
			return -1;
		}

		player.sendSystemMessage(Component.literal("Claiming chunk"));
		return TeamUtils.claimCurrentChunk(player);
	}

	@Override
    public boolean canExecute(Player player) {
        setMustWhat("be a player and be in a team");
        return TeamUtils.hasTeam(player);
    }
}