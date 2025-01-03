package com.createcivilization.capitol.command.custom.teamcommands.team;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wiiu.mavity.util.ObjectHolder;

import java.awt.Color;

public class CreateTeamCommand extends AbstractTeamCommand {

    public CreateTeamCommand() {
        super("createTeam");
        command = Commands.literal(subCommandName).requires(this::canExecuteAllParams).executes((c) -> 1)
                .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("color", StringArgumentType.word())
                                .executes((command) -> {
                                    String
                                            color = StringArgumentType.getString(command, "color").toLowerCase(),
                                            name = StringArgumentType.getString(command, "name");
									if (TeamUtils.teamExists(name)) {
										command.getSource().sendFailure(Component.literal("A team with the name '" + name + "' already exists!"));
										return -1;
									}
                                    return new ObjectHolder<Player>(command.getSource().getPlayer()).ifPresentOrElse(player -> {
                                        try {
                                            TeamUtils.loadedTeams.add(TeamUtils.createTeam(name, player, (Color)  Color.class.getField(color).get(null)));
                                            command.getSource().sendSuccess(() -> Component.literal("Created team '" + name + "' with color '" + color + "'."), true);
											command.getSource().sendSystemMessage(Component.literal("Please leave and rejoin the server or world you are playing so you can access the right commands."));
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
                                })
                        )
                );
    }

	@Override
    public boolean canExecute(Player player) {
        setMustWhat("be a player and not be in a team");
        return !TeamUtils.hasTeam(player);
    }
}