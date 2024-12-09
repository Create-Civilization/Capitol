package com.createcivilization.capitol.command.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;

public class AbstractCommand {

    private final String commandName;

    public AbstractCommand(String commandName) {
        this.commandName = commandName;
    }

    public boolean canExecute(CommandSourceStack s) {
        return true;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(commandName).requires(this::canExecute)
                .executes(this::execute)
        );
    }

    public int execute(CommandContext<CommandSourceStack> command) {
        return 1;
    }
}