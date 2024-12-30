package com.createcivilization.capitol.command.custom.teamcommands.chunks;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class ClaimCurrentChunkCommand extends AbstractTeamCommand {

    public ClaimCurrentChunkCommand() {
        super("claimCurrentChunk");
        command = CommandManager.literal(subCommandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
    }

	@Override
	public int execute(PlayerEntity player) {
		if (TeamUtils.isInClaimedChunk(player)) {
			// Chunk already claimed
			player.sendMessage(Text.literal("Chunk already claimed"));
			return -1;
		}
		if (!TeamUtils.nearClaimedChunk(player.getChunkPos(), 1, player)) {
			// Near chunk to extend
			player.sendMessage(Text.literal("Must be near a claimed chunk"));
			return -1;
		}

		player.sendMessage(Text.literal("Claiming chunk"));
		return TeamUtils.claimCurrentChunk(player);
	}

	@Override
    public boolean canExecute(PlayerEntity player) {
        setMustWhat("be a player and be in a team");
        return TeamUtils.hasTeam(player);
    }
}