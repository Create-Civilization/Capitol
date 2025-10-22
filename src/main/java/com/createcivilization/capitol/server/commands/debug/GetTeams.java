package com.createcivilization.capitol.server.commands.debug;

import com.createcivilization.capitol.common.assets.Team;
import com.createcivilization.capitol.common.data.TeamData;
import com.createcivilization.capitol.server.commands.abstracts.OperatorCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * Debug command that returns all the teams loaded in the server.
 */
public class GetTeams extends OperatorCommand {
	public GetTeams() {
		super("getTeams");
	}

	@Override
	public int executes(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSystemMessage(Component.literal(TeamData.getTeams().stream().map(Team::toReducedString).toList().toString()));
		return 1;
	}
}
