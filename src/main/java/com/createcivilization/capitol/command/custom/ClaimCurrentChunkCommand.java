package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.util.TeamUtils;

import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class ClaimCurrentChunkCommand extends AbstractTeamCommand {

    public ClaimCurrentChunkCommand() {
        super("claimCurrentChunk");
        command = Commands.literal(commandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
    }

	@Override
	public int execute(Player player) {
		return TeamUtils.claimCurrentChunk(player);
	}

	@Override
    public boolean canExecute(Player player) {
        setMustWhat("be a player and be in a team");
        return TeamUtils.hasTeam(player);
    }
}