package com.createcivilization.capitol.command.custom.abstracts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.Text;

import wiiu.mavity.util.ObjectHolder;

public abstract class AbstractTeamCommand extends AbstractCommand {

    protected ArgumentBuilder<ServerCommandSource, ?> command;

    protected AbstractTeamCommand(String commandName) {
        super(commandName);
    }

    protected String mustWhat = "be a player";

	//What the user must have to do the command, "be a player", "be a player and an operator", "be a player and in a team"
    public void setMustWhat(String mustWhat) {
        this.mustWhat = mustWhat;
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("capitolTeams").requires((command) -> {
            if (!command.isExecutedByPlayer()) {
                command.sendError(Text.literal("You must be a player to execute this command!"));
                return false;
            } else return !command.getWorld().isClient();
        }).then(this.command));
    }

	// See AbstractCommand
    @Override
    public boolean canExecuteAllParams(ServerCommandSource s) {
        return new ObjectHolder<>(s.getPlayer()).ifPresentOrElse(this::canExecute, () -> false);
    }

	// Executes all Parameters passing CommandSourceStack
    @Override
    public int executeAllParams(CommandContext<ServerCommandSource> command) {
        ObjectHolder<PlayerEntity> playerObject = new ObjectHolder<>(command.getSource().getPlayer());
        return playerObject.ifPresentOrElse(this::execute, () -> {
            command.getSource().sendError(Text.literal("You must " + this.mustWhat + " to use this command."));
            return -1;
        });
    }

	// Executes command passing player
    public int execute(PlayerEntity player) {
		return 1;
	}

    public boolean canExecute(PlayerEntity player) {
		return true;
	}
}