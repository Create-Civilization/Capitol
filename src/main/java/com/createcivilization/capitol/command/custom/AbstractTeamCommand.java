package com.createcivilization.capitol.command.custom;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wiiu.mavity.util.ObjectHolder;

public abstract class AbstractTeamCommand extends AbstractCommand {

    protected AbstractTeamCommand(String commandName) {
        super(commandName);
    }

    @Override
    public boolean canExecute(CommandSourceStack s) {
        ObjectHolder<Player> playerObject = new ObjectHolder<>(s.getPlayer());
        playerObject.ifPresentOrElse(this::canExecute, () -> s.sendFailure(Component.literal("You must be a player to use this command.")));
        return super.canExecute(s);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> command) {
        ObjectHolder<Player> playerObject = new ObjectHolder<>(command.getSource().getPlayer());
        playerObject.ifPresentOrElse(this::canExecute, () -> command.getSource().sendFailure(Component.literal("You must be a player to use this command.")));
        return 0;
    }

    public abstract int execute(Player player);

    public abstract void canExecute(Player player);
}