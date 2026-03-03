package com.createcivilization.capitol.old.server.commands.abstracts;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

/**
 * {@link Command} that requires operator permissions.
 */
public abstract class OperatorCommand extends Command {
	public OperatorCommand(String name) {
		super(name);
	}

	@Override
	public boolean requires(CommandSourceStack commandSourceStack) {
		return commandSourceStack.hasPermission(0);
	}

	@Override
	protected LiteralArgumentBuilder<CommandSourceStack> setup() {
		return super.setup();
	}
}
