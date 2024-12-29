package com.createcivilization.capitol.command.custom.teamcommands.team;

import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

import wiiu.mavity.util.ObjectHolder;

import java.awt.Color;

public class CreateTeamCommand extends AbstractTeamCommand {

    public CreateTeamCommand() {
        super("createTeam");
        command = CommandManager.literal(commandName).requires(this::canExecuteAllParams).executes((c) -> 1)
                .then(CommandManager.argument("name", StringArgumentType.string())
                        .then(CommandManager.argument("color", StringArgumentType.word())
                                .executes((command) -> {
                                    String
                                            color = StringArgumentType.getString(command, "color").toLowerCase(),
                                            name = StringArgumentType.getString(command, "name");
									if (TeamUtils.teamExists(name)) {
										command.getSource().sendError(Text.literal("A team with the name '" + name + "' already exists!"));
										return -1;
									}
                                    return new ObjectHolder<PlayerEntity>(command.getSource().getPlayer()).ifPresentOrElse(player -> {
                                        try {
                                            TeamUtils.loadedTeams.add(TeamUtils.createTeam(name, player, (Color) Color.class.getDeclaredField(color).get(null)));
                                            command.getSource().sendFeedback(() -> Text.literal("Created team '" + name + "' with color '" + color + "'."), true);
											command.getSource().sendMessage(Text.literal("Please leave and rejoin the server or world you are playing so you can access the right commands."));
                                            return 1;
                                        } catch (Throwable e) {
                                            e.printStackTrace(System.out);
                                            e.printStackTrace(System.err);
                                            command.getSource().sendError(Text.literal("Invalid color: '" + color + "'!"));
                                            return -1;
                                        }
                                    }, () -> {
                                        command.getSource().sendError(Text.literal("You must " + this.mustWhat + " to use this command."));
                                        return -1;
                                    });
                                })
                        )
                );
    }

	@Override
    public boolean canExecute(PlayerEntity player) {
        setMustWhat("be a player and not be in a team");
        return !TeamUtils.hasTeam(player);
    }
}