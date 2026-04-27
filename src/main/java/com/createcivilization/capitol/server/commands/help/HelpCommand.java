package com.createcivilization.capitol.server.commands.help;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Optional;

public class HelpCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("help").executes(HelpCommand::showHelp);
	}

	private static int showHelp(CommandContext<CommandSourceStack> context) {
		MutableComponent msg = Component.literal("=== Capitol Commands ===\n").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);

		msg.append(entry("/capitol help", "Show this help message"));
		msg.append(entry("/capitol claim chunk", "Claim the chunk you're standing in"));
		msg.append(entry("/capitol claim sub_level", "Claim the sub-level you're in"));
		msg.append(entry("/capitol claim info", "Show who owns the current chunk"));
		msg.append(entry("/capitol claim info sub_level", "Show who owns the current sub-level"));
		msg.append(entry("/capitol unclaim chunk", "Unclaim the chunk you're standing in"));
		msg.append(entry("/capitol unclaim sub_level", "Unclaim the sub-level you're in"));
		msg.append(entry("/capitol team create <name> <tag> <color> [desc]", "Create a new team"));
		msg.append(entry("/capitol team info", "View your team's details"));
		msg.append(entry("/capitol team invite <player>", "Invite a player to your team"));
		msg.append(entry("/capitol team kick <player>", "Kick a player from your team"));
		msg.append(entry("/capitol team disband", "Disband your team"));
		msg.append(entry("/capitol team role <name> create|remove|rename|assign|permission", "Manage team roles"));
		msg.append(entry("/capitol team protection <name>", "Toggle a claim protection setting"));
		msg.append(entry("/capitol invites <team> accept|deny", "Accept or deny a team invite", false));

		context.getSource().sendSuccess(() -> msg, false);
		return 1;
	}

	private static MutableComponent entry(String command, String description) {
		return entry(command, description, true);
	}

	private static MutableComponent entry(String command, String description, boolean newline) {
		String newLine = newline ? "\n" : "";
		return Component.empty()
			.append(Component.literal(command).withStyle(ChatFormatting.AQUA).withStyle(s -> s.withBold(false)))
			.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY).withStyle(s -> s.withBold(false)))
			.append(Component.literal(description).withStyle(ChatFormatting.WHITE).withStyle(s -> s.withBold(false)))
			.append(Component.literal("\n"));
	}
}
