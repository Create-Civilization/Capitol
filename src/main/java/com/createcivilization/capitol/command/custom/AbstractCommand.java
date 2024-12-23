package com.createcivilization.capitol.command.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;

public abstract class AbstractCommand {

    protected final String commandName;


    protected AbstractCommand(String commandName) {
        this.commandName = commandName;
    }
	// Method to check if all parameters can be executed
    public boolean canExecuteAllParams(CommandSourceStack s) {
        return true;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(commandName).requires(this::canExecuteAllParams)
                .executes(this::executeAllParams)
        );
    }

    public abstract int executeAllParams(CommandContext<CommandSourceStack> command);
}