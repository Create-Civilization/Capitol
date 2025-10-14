package com.createcivilization.capitol.server.commands.teams;

import com.createcivilization.capitol.common.classes.Request;
import com.createcivilization.capitol.common.data.TeamData;
import com.createcivilization.capitol.server.commands.abstracts.TeamCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Objects;

import static com.createcivilization.capitol.server.ServerConstants.SERVER_UUID;

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
		TeamData.createTeam(
			new Request(source.isPlayer() ? Objects.requireNonNull(source.getPlayer()).getUUID() : SERVER_UUID),
			"team"
		);
		return 1;
	}
}
