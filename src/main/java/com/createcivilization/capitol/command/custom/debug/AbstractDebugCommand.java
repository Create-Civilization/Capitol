package com.createcivilization.capitol.command.custom.debug;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;

import net.minecraft.commands.*;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

public class AbstractDebugCommand extends AbstractTeamCommand {

	protected ObjectHolder<ArgumentBuilder<CommandSourceStack, ?>> subSubCommand = new ObjectHolder<>();

	protected AbstractDebugCommand() {
		super("debug");
	}

	@Override
	public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		command.set(
			Commands.literal(subCommandName.getOrThrow())
				.requires(this::canExecuteAllParams)
				.then(subSubCommand.getOrThrow())
		);
		super.register(dispatcher);
	}
}