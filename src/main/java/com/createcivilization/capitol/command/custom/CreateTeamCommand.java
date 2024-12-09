package com.createcivilization.capitol.command.custom;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

public class CreateTeamCommand extends AbstractCommand {

    public CreateTeamCommand() {
        super("createTeam");
    }

    @Override
    public boolean canExecute(CommandSourceStack s) {
        return super.canExecute(s);
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> command) {
        @Nullable Entity e = command.getSource().getEntity();
        if (e != null) {
            if (e instanceof Player player) {
                player.sendSystemMessage(Component.literal("Hello World")); // Test response
                return 1;
            }
        }
        return -1;
    }
}