package com.createcivilization.capitol.server.utils;

import com.createcivilization.capitol.common.assets.Status;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * Collection of handlers for statuses.
 */
public class StatusHandlers {

	/**
	 * Handles statuses as a command response.
	 * <p>
	 * Will send a success/failure message to the source with the summary (if failed).
	 * @param status The status of the operation.
	 * @param ctx The context of the command.
	 */
	public static void CommandHandler(Status status, CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		if (status.success())
			source.sendSuccess(() -> Component.literal(status.summary()), true);
		else
			source.sendFailure(Component.literal(status.summary() + (status.description() != null ? "\n" + status.description() : "")));
	}
}
