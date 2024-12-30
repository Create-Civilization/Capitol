package com.createcivilization.capitol.command.custom.abstracts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.*;

public abstract class AbstractCommand {

    protected final String commandName;

    protected AbstractCommand(String commandName) {
        this.commandName = commandName;
    }

	// Method to check if all parameters can be executed
    public boolean canExecuteAllParams(ServerCommandSource s) {
        return true;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(commandName).requires(this::canExecuteAllParams)
                .executes(this::executeAllParams)
        );
    }

    public abstract int executeAllParams(CommandContext<ServerCommandSource> command);
}