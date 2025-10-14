package com.createcivilization.capitol.server.commands.abstracts;

import net.minecraft.commands.CommandSourceStack;

public abstract class OperatorCommand extends Command {
	public OperatorCommand(String name) {
		super(name);
	}

	@Override
	public boolean requires(CommandSourceStack commandSourceStack) {
		return commandSourceStack.hasPermission(0);
	}
}
