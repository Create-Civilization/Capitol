package com.createcivilization.capitol.server.commands.abstracts;

import com.createcivilization.capitol.common.data.TeamData;
import net.minecraft.commands.CommandSourceStack;

import java.util.Objects;

public abstract class TeamCommand extends Command {
	public TeamCommand(String name) {
		super(name);
	}

	@Override
	public boolean requires(CommandSourceStack commandSourceStack) {
		return commandSourceStack.isPlayer() && TeamData.BaseUtils.playerHasTeam(Objects.requireNonNull(commandSourceStack.getPlayer()).getUUID());
	}
}
