package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.CommandDispatcher;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wiiu.mavity.util.ObjectHolder;

import java.awt.*;

import static net.minecraft.commands.Commands.argument;

public class CreateTeamCommand extends AbstractTeamCommand {

    public CreateTeamCommand() {
        super("createTeam", );
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ObjectHolder<String> name = new ObjectHolder<>();
        dispatcher.register(Commands.literal(commandName).requires(this::canExecuteAllParams)
                .then(argument("name", StringArgumentType.greedyString())
                        .executes((command) -> {
                            name.set(StringArgumentType.getString(command, "name"));
                            return -1;
                        })).then(argument("color", StringArgumentType.word()))
                                .executes((command) -> {
                                    String color = StringArgumentType.getString(command, "color");
                                    return new ObjectHolder<Player>().ifPresentOrElse(player -> {
                                        try {
                                            var localName = name.get();
                                            TeamUtils.loadedTeams.add(TeamUtils.createTeam(name.get(), player, (Color) Color.class.getDeclaredField(color).get(null)));
                                            return 1;
                                        } catch (Throwable e) {
                                            command.getSource().sendFailure(Component.literal("Invalid color: '" + color + "'!"));
                                            return -1;
                                        }
                                    }, () -> {
                                        command.getSource().sendFailure(Component.literal("You must " + this.mustWhat + " to use this command."));
                                        return -1;
                                    });
                                })
        );
    }

    @Override
    public int execute(Player player) {
        return 1;
    }

    @Override
    public boolean canExecute(Player player) {
        setMustWhat("be a player and not be in a team");
        return !TeamUtils.hasTeam(player);
    }
}