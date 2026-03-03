package com.createcivilization.capitol.old.server.commands.teams;

import com.createcivilization.capitol.old.common.assets.Request;
import com.createcivilization.capitol.old.common.data.TeamData;
import com.createcivilization.capitol.old.server.ServerConstants;
import com.createcivilization.capitol.old.server.commands.abstracts.TeamCommand;
import com.createcivilization.capitol.old.server.utils.StatusHandlers;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.UUID;

public class DisbandTeam extends TeamCommand {

	public DisbandTeam() {
		super("disbandTeam");
	}

	@Override
	public boolean requires(CommandSourceStack commandSourceStack) {
		return !commandSourceStack.isPlayer() || commandSourceStack.hasPermission(0) || super.requires(commandSourceStack);
	}

	@Override
	public int executes(CommandContext<CommandSourceStack> context) {
		String string = StringArgumentType.getString(context, "teamId");
		StatusHandlers.CommandHandler(
			TeamData.SmartUtils.disbandTeam(
				new Request(ServerConstants.resolveUUIDFromSource.apply(context.getSource())),
				string == null ? null : UUID.fromString(string)
			),
			context
		);
		return 1;
	}

	@Override
	protected LiteralArgumentBuilder<CommandSourceStack> setup() {
		return Commands.literal(this.name)
		.requires(this::requires)
		.executes(this::executes)
		.then(
			Commands.argument(
				"teamId",
				StringArgumentType.word()
			)
			.requires(commandSourceStack -> commandSourceStack.hasPermission(0))
			.executes(this::executes)
		);
	}
}
