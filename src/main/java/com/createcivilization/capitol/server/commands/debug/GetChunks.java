package com.createcivilization.capitol.server.commands.debug;

import com.createcivilization.capitol.common.data.ClaimData;
import com.createcivilization.capitol.server.commands.abstracts.OperatorCommand;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * Debug command that returns all the chunks loaded in the server.
 */
public class GetChunks extends OperatorCommand {

	public GetChunks() {
		super("getChunks");
	}

	@Override
	public int executes(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSystemMessage(Component.literal(ClaimData.BaseUtils.getChunks()));
		return 1;
	}
}
