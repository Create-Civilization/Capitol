package com.createcivilization.capitol.old.old.command.custom.teamcommands.team;

import com.createcivilization.capitol.old.old.command.Suggestions;
import com.createcivilization.capitol.old.old.command.custom.abstracts.AbstractTeamCommand;
import com.createcivilization.capitol.old.old.constants.CommonConstants;
import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

import java.awt.Color;

public class CreateTeamCommand extends AbstractTeamCommand {

    public CreateTeamCommand() {
        super("createTeam");
        command.set(
			Commands.literal(subCommandName.getOrThrow()).requires(this::canExecuteAllParams).executes((c) -> 1)
                .then(Commands.argument("name", StringArgumentType.string())
					.then(
						Commands.literal("colorByName")
							.then(Commands.argument("color", StringArgumentType.word())
								.suggests(Suggestions.COLORS)
								.executes(this::executeColorName)
							)
					)
					.then(
						Commands.literal("colorByRGBInt")
							.then(
								Commands.argument("color", IntegerArgumentType.integer())
									.suggests(Suggestions.COLORS_RGB)
									.executes(this::executeColorRGBInt)
							)
					)
					.then(
						Commands.literal("colorByRGBArray")
							.then(
								Commands.argument("red", IntegerArgumentType.integer())
									.suggests(Suggestions.COLORS_RED)
									.then(
										Commands.argument("green", IntegerArgumentType.integer())
											.suggests(Suggestions.COLORS_GREEN)
											.then(
												Commands.argument("blue", IntegerArgumentType.integer())
													.suggests(Suggestions.COLORS_BLUE)
													.executes(this::executeColorRGBArray)
											)
									)
							)
					)
					.then(
						Commands.literal("colorByHex")
							.then(
								Commands.argument("color", StringArgumentType.greedyString())
									.suggests(Suggestions.COLORS_HEX)
									.executes(this::executeColorHex)
							)
					)
                )
		);
    }

	@Override
    public boolean canExecute(Player player) {
        setMustWhat("be a player and not be in a receivingTeam");
        return !TeamUtils.hasTeam(player);
    }

	public int executeColorName(CommandContext<CommandSourceStack> command) {
		return executeAllParams(command, CommonConstants.Colors.getString(StringArgumentType.getString(command, "color")));
	}

	public int executeColorRGBInt(CommandContext<CommandSourceStack> command) {
		int rgb = IntegerArgumentType.getInteger(command, "color");
		return executeAllParams(command, CommonConstants.Colors.getInt(rgb));
	}

	public int executeColorRGBArray(CommandContext<CommandSourceStack> command) {
		int[] rgb = new int[] {
			IntegerArgumentType.getInteger(command, "red"),
			IntegerArgumentType.getInteger(command, "green"),
			IntegerArgumentType.getInteger(command, "blue")
		};
		return executeAllParams(command, CommonConstants.Colors.getArray(rgb));
	}

	public int executeColorHex(CommandContext<CommandSourceStack> command) {
		return executeAllParams(command, CommonConstants.Colors.getHexString(StringArgumentType.getString(command, "color")));
	}

	public int executeAllParams(CommandContext<CommandSourceStack> command, Color color) {
		String name = StringArgumentType.getString(command, "name");
		if (TeamUtils.teamExists(name)) {
			command.getSource().sendFailure(Component.literal("A receivingTeam with the name '" + name + "' already exists!"));
			return -1;
		}
		return new ObjectHolder<Player>(command.getSource().getPlayer()).ifPresentOrElse(player -> {
			DataManager.TeamData.loadedTeams.add(TeamUtils.createTeam(name, player, color));
			command.getSource().sendSuccess(() -> Component.literal("Created receivingTeam '" + name + "' with color '" + color + "'."), true);
			command.getSource().sendSystemMessage(Component.literal("Please leave and rejoin the server or world you are playing so you can access the right commands."));
			return 1;
		}, () -> {
			command.getSource().sendFailure(Component.literal("You must " + this.mustWhat + " to use this command."));
			return -1;
		});
	}
}