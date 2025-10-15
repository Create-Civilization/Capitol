package com.createcivilization.capitol.server.commands.abstracts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * A command helper abstraction.
 * <p>
 * requires - Predicate to identify requirements to perform the command.
 * executes - Factory that executes the command returns 1 for success and 0 for failure.
 */
public abstract class Command {
	public final String name;

	public Command(String name) {
		this.name = name;
	}

	public abstract boolean requires(CommandSourceStack commandSourceStack);
	public abstract int executes(CommandContext<CommandSourceStack> commandSourceStackCommandContext);

	public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			Commands.literal(this.name)
			.requires(this::requires)
			.executes(this::executes)
		);
	}
}
