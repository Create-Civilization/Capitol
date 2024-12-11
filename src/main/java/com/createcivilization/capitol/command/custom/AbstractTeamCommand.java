package com.createcivilization.capitol.command.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wiiu.mavity.util.ObjectHolder;

public abstract class AbstractTeamCommand extends AbstractCommand {

    protected ArgumentBuilder<CommandSourceStack, ?> command;

    protected AbstractTeamCommand(String commandName) {
        super(commandName);
    }

    protected String mustWhat = "be a player";

    public void setMustWhat(String mustWhat) {
        this.mustWhat = mustWhat;
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("capitolTeams").requires((command) -> {
            if (!command.isPlayer()) {
                command.sendFailure(Component.literal("You must be a player to execute this command!"));
                return false;
            } else return !command.getLevel().isClientSide();
        }).then(this.command));
    }

    @Override
    public boolean canExecuteAllParams(CommandSourceStack s) {
        return new ObjectHolder<>(s.getPlayer()).ifPresentOrElse(this::canExecute, () -> false);
    }

    @Override
    public int executeAllParams(CommandContext<CommandSourceStack> command) {
        ObjectHolder<Player> playerObject = new ObjectHolder<>(command.getSource().getPlayer());
        return playerObject.ifPresentOrElse(this::execute, () -> {
            command.getSource().sendFailure(Component.literal("You must " + this.mustWhat + " to use this command."));
            return -1;
        });
    }

    public abstract int execute(Player player);

    public abstract boolean canExecute(Player player);
}