package com.createcivilization.capitol.server.commands;

import com.createcivilization.capitol.common.assets.Status;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class CommandStatusHandler{
	public CommandStatusHandler(Status status, CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		if (status.success())
			source.sendSuccess(() -> Component.literal(status.summary()), true);
		else
			source.sendFailure(Component.literal(status.summary() + (status.description() != null ? "\n" + status.description() : "")));
	}
}
