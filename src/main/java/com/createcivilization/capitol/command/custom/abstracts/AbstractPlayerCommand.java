package com.createcivilization.capitol.command.custom.abstracts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.*;
import net.minecraft.text.Text;

import org.jetbrains.annotations.Nullable;

import wiiu.mavity.util.ObjectHolder;

public abstract class AbstractPlayerCommand extends AbstractCommand {

	protected @Nullable ArgumentBuilder<ServerCommandSource, ?> command;
	protected @Nullable String subCommandName;

	protected AbstractPlayerCommand(String commandName, @Nullable String subCommandName) {
		super(commandName);
		this.subCommandName = subCommandName;
	}

	protected String mustWhat = "be a player";

	// What the user must have to do the command, i.e "be a player", "be a player and an operator", "be a player and in a team"
	public void setMustWhat(String mustWhat) {
		this.mustWhat = mustWhat;
	}

	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal(this.commandName).requires((command) -> {
			if (!command.isExecutedByPlayer()) {
				command.sendError(Text.literal("You must be a player to execute this command!"));
				return false;
			} else return this.canExecuteBaseCommand(command);
		}).then(new ObjectHolder<>(this.command).ifPresentOrElse(
				(command) -> command,
				() -> CommandManager.literal(this.subCommandName).requires(this::canExecuteAllParams).executes(this::executeAllParams))
			)
		);
	}

	public boolean canExecuteBaseCommand(ServerCommandSource command) {
		return !command.getWorld().isClient();
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