package com.createcivilization.capitol.command.custom.abstracts;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

public abstract class AbstractPlayerCommand extends AbstractCommand {

	protected final ObjectHolder<ArgumentBuilder<CommandSourceStack, ?>> command = new ObjectHolder<>();
	protected final ObjectHolder<String> subCommandName = new ObjectHolder<>();

	protected AbstractPlayerCommand(String commandName, @Nullable String subCommandName) {
		super(commandName);
		this.subCommandName.set(subCommandName);
	}

	protected String mustWhat = "be a player";

	// What the user must have to do the command, i.e "be a player", "be a player and an operator", "be a player and in a team"
	public void setMustWhat(String mustWhat) {
		this.mustWhat = mustWhat;
	}

	@Override
	public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal(this.commandName).requires((command) -> {
				if (!command.isPlayer()) {
					command.sendFailure(Component.literal("You must be a player to execute this command!"));
					return false;
				} else return this.canExecuteBaseCommand(command);
			}).then(this.command.ifPresentOrElse(
				(command) -> command,
				() -> Commands.literal(this.subCommandName.getOrThrow()).requires(this::canExecuteAllParams).executes(this::executeAllParams))
			)
		);
	}

	public boolean canExecuteBaseCommand(CommandSourceStack command) {
		return !command.getLevel().isClientSide();
	}

	// See AbstractCommand
	@Override
	public boolean canExecuteAllParams(CommandSourceStack s) {
		return new ObjectHolder<>(s.getPlayer()).ifPresentOrElse(this::canExecute, () -> false);
	}

	// Executes all Parameters passing CommandSourceStack
	@Override
	public int executeAllParams(CommandContext<CommandSourceStack> command) {
		return new ObjectHolder<>(command.getSource().getPlayer()).ifPresentOrElse(this::execute, () -> {
			command.getSource().sendFailure(Component.literal("You must " + this.mustWhat + " to use this command."));
			return -1;
		});
	}

	// Executes command passing player
	public int execute(Player player) {
		return 1;
	}

	public boolean canExecute(Player player) {
		return true;
	}
}