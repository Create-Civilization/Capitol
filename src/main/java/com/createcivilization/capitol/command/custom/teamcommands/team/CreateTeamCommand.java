package com.createcivilization.capitol.command.custom.teamcommands.team;

import com.createcivilization.capitol.command.Suggestions;
import com.createcivilization.capitol.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.constants.CommonConstants;
import com.createcivilization.capitol.util.TeamUtils;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

public class CreateTeamCommand extends AbstractTeamCommand {

    public CreateTeamCommand() {
        super("createTeam");
        command.set(
			Commands.literal(subCommandName.getOrThrow()).requires(this::canExecuteAllParams).executes((c) -> 1)
                .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("color", StringArgumentType.word())
								.suggests(Suggestions.COLORS)
                                .executes((command) -> {
                                    String
                                            color = StringArgumentType.getString(command, "color").toUpperCase(),
                                            name = StringArgumentType.getString(command, "name");
									if (TeamUtils.teamExists(name)) {
										command.getSource().sendFailure(Component.literal("A team with the name '" + name + "' already exists!"));
										return -1;
									}
                                    return new ObjectHolder<Player>(command.getSource().getPlayer()).ifPresentOrElse(player -> {
										TeamUtils.loadedTeams.add(TeamUtils.createTeam(name, player, CommonConstants.colors.get(color)));
										command.getSource().sendSuccess(() -> Component.literal("Created team '" + name + "' with color '" + color + "'."), true);
										command.getSource().sendSystemMessage(Component.literal("Please leave and rejoin the server or world you are playing so you can access the right commands."));
										return 1;
                                    }, () -> {
                                        command.getSource().sendFailure(Component.literal("You must " + this.mustWhat + " to use this command."));
                                        return -1;
                                    });
                                })
                        )
                )
		);
    }

	@Override
    public boolean canExecute(Player player) {
        setMustWhat("be a player and not be in a team");
        return !TeamUtils.hasTeam(player);
    }
}