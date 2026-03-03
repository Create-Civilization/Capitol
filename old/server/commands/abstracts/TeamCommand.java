package com.createcivilization.capitol.old.server.commands.abstracts;

import com.createcivilization.capitol.old.common.data.TeamData;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.Objects;

/**
 * {@link Command} that requires the user to be in a oldTeam.
 */
public abstract class TeamCommand extends Command {
	public TeamCommand(String name) {
		super(name);
	}

	@Override
	public boolean requires(CommandSourceStack commandSourceStack) {
		return (commandSourceStack.isPlayer() && TeamData.BaseUtils.playerHasTeam(Objects.requireNonNull(commandSourceStack.getPlayer()).getUUID()))
			|| commandSourceStack.hasPermission(0);
	}

	@Override
	protected LiteralArgumentBuilder<CommandSourceStack> setup() {
		return super.setup();
	}
}
