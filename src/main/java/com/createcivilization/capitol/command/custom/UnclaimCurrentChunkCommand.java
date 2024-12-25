package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class UnclaimCurrentChunkCommand extends AbstractTeamCommand {

    public UnclaimCurrentChunkCommand() {
        super("unclaimCurrentChunk");
        command = Commands.literal(commandName).requires(this::canExecuteAllParams).executes(this::executeAllParams);
    }

	@Override
	public int execute(Player player)
	{
		if (!TeamUtils.isInClaimedChunk(player))
		{
			// Chunk already claimed
			player.sendSystemMessage(Component.literal("Chunk not claim claimed"));
			return -1;
		}
		if (!TeamUtils.allowedInChunk(player, player.chunkPosition())){

			// Player is not allowed to unclaim chunk
			player.sendSystemMessage(Component.literal("You are not allowed to unclaim this chunk"));
			return -1;
		}
		if (TeamUtils.hasCapitol(player.chunkPosition(), player.level().dimension().location())){

			// Please add a proper check
			player.sendSystemMessage(Component.literal("You are not allowed to unclaim a chunk with a capitolblock in it"));
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