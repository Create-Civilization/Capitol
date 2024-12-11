package com.createcivilization.capitol.command.custom;

import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wiiu.mavity.util.ObjectHolder;

import java.awt.Color;

public class CreateTeamCommand extends AbstractTeamCommand {

    public CreateTeamCommand() {
        super("createTeam");
        ObjectHolder<String> name = new ObjectHolder<>();
        command = Commands.literal(commandName).requires(this::canExecuteAllParams)
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes((command) -> {
                            name.set(StringArgumentType.getString(command, "name"));
                            return 1;
                        })
                .then(Commands.argument("color", StringArgumentType.string()))
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
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("capitolTeams").requires((command) -> {
            if (!command.isPlayer()) {
                command.sendFailure(Component.literal("You must be a player to execute this command!"));
                return false;
            } else return !command.getLevel().isClientSide();
        }).executes((c) -> 1).then(Commands.literal(commandName).requires(this::canExecuteAllParams).executes((c) -> 1)
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes((command) -> 1)
                        .then(Commands.argument("color", StringArgumentType.word())
                                .executes((command) -> {
                                    String color = StringArgumentType.getString(command, "color").toLowerCase();
                                    String name = StringArgumentType.getString(command, "name");
                                    return new ObjectHolder<Player>(command.getSource().getPlayer()).ifPresentOrElse(player -> {
                                        try {
                                            System.out.println("Success");
                                            TeamUtils.loadedTeams.add(TeamUtils.createTeam(name, player, (Color) Color.class.getDeclaredField(color).get(null)));
                                            command.getSource().sendSuccess(() -> Component.literal("Created team '" + name + "' with color '" + color + "'."), true);
                                            return 1;
                                        } catch (Throwable e) {
                                            e.printStackTrace(System.out);
                                            e.printStackTrace(System.err);
                                            command.getSource().sendFailure(Component.literal("Invalid color: '" + color + "'!"));
                                            return -1;
                                        }
                                    }, () -> {
                                        System.out.println("Failure ;L");
                                        command.getSource().sendFailure(Component.literal("You must " + this.mustWhat + " to use this command."));
                                        return -1;
                                    });
                                }))
                ))
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