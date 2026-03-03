package com.createcivilization.capitol.old.server.commands.teams;

import com.createcivilization.capitol.old.common.assets.Request;
import com.createcivilization.capitol.old.common.data.TeamData;
import com.createcivilization.capitol.old.server.ServerConstants;
import com.createcivilization.capitol.old.server.utils.StatusHandlers;
import com.createcivilization.capitol.old.server.commands.abstracts.TeamCommand;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Creates a team,
 * <p>
 * If ran by a player creates a team with them as an owner,
 * <p>
 * If ran by the server creates an empty slate of a team with no members.
 */
public class CreateTeam extends TeamCommand {

	public CreateTeam() {
		super("createTeam");
	}

	@Override
	public boolean requires(CommandSourceStack commandSourceStack) {
		return !super.requires(commandSourceStack) || commandSourceStack.hasPermission(0);
	}

	public int executes(CommandContext<CommandSourceStack> ctx) {
		StatusHandlers.CommandHandler(
			TeamData.SmartUtils.createTeam(
				new Request(ServerConstants.resolveUUIDFromSource.apply(ctx.getSource())),
				StringArgumentType.getString(ctx, "name")
			),
			ctx
		);
		return 1;
	}

	@Override
	public LiteralArgumentBuilder<CommandSourceStack> setup() {
		return Commands.literal(this.name)
		.then(
			Commands.argument(
				"name",
				StringArgumentType.word()
			)
			.requires(this::requires)
			.executes(this::executes)
		);
	}
}
