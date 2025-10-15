package com.createcivilization.capitol.server.commands.teams;

import com.createcivilization.capitol.common.assets.Request;
import com.createcivilization.capitol.common.data.TeamData;
import com.createcivilization.capitol.server.utils.StatusHandlers;
import com.createcivilization.capitol.server.commands.abstracts.TeamCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.Objects;

import static com.createcivilization.capitol.server.ServerConstants.SERVER_UUID;

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
		return !super.requires(commandSourceStack);
	}

	@Override
	public int executes(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		StatusHandlers.CommandHandler(
			TeamData.SmartUtils.createTeam(
				new Request(source.isPlayer() ? Objects.requireNonNull(source.getPlayer()).getUUID() : SERVER_UUID),
				"team"
			),
			ctx
		);
		return 1;
	}
}
