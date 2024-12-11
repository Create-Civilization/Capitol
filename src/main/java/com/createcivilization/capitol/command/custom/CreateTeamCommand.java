package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wiiu.mavity.util.ObjectHolder;

import java.awt.Color;

import static net.minecraft.commands.Commands.argument;

public class CreateTeamCommand extends AbstractTeamCommand {

    public CreateTeamCommand() {
        super("createTeam");
        ObjectHolder<String> name = new ObjectHolder<>();
        command = Commands.literal(commandName).requires(this::canExecuteAllParams)
                .then(argument("name", StringArgumentType.string())
                        .executes((command) -> {
                            name.set(StringArgumentType.getString(command, "name"));
                            return 1;
                        })
                .then(argument("color", StringArgumentType.string()))
                .executes((command) -> {
                    String color = StringArgumentType.getString(command, "color");
                    return new ObjectHolder<Player>().ifPresentOrElse(player -> {
                        try {
                            var localName = name.get();
                            TeamUtils.loadedTeams.add(TeamUtils.createTeam(name.get(), player, (Color) Color.class.getDeclaredField(color).get(null)));
                            command.getSource().sendSuccess(() -> Component.literal("Created team '" + localName + "' with color '" + color + "'."), true);
                            return 1;
                        } catch (Throwable e) {
                            e.printStackTrace(System.out);
                            e.printStackTrace(System.err);
                            command.getSource().sendFailure(Component.literal("Invalid color: '" + color + "'!"));
                            return -1;
                        }
                    }, () -> {
                        command.getSource().sendFailure(Component.literal("You must " + this.mustWhat + " to use this command."));
                        return -1;
                    });
                }));
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