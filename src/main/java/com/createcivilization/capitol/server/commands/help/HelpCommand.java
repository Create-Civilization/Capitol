package com.createcivilization.capitol.server.commands.help;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class HelpCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("help").executes(HelpCommand::showHelp);
	}

	private static int showHelp(CommandContext<CommandSourceStack> context) {
		MutableComponent msg = Component.translatable("commands.capitol.help.title").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
		msg.append(Component.literal("\n"));

		msg.append(entry("/capitol help", Component.translatable("commands.capitol.help.help")));
		msg.append(entry("/capitol claim chunk", Component.translatable("commands.capitol.help.claim.chunk")));
		msg.append(entry("/capitol claim sub_level", Component.translatable("commands.capitol.help.claim.sub_level")));
		msg.append(entry("/capitol claim info", Component.translatable("commands.capitol.help.claim.info")));
		msg.append(entry("/capitol claim info sub_level", Component.translatable("commands.capitol.help.claim.info.sub_level")));
		msg.append(entry("/capitol claim auto", Component.translatable("commands.capitol.help.claim.auto")));
		msg.append(entry("/capitol unclaim chunk", Component.translatable("commands.capitol.help.unclaim.chunk")));
		msg.append(entry("/capitol unclaim sub_level", Component.translatable("commands.capitol.help.unclaim.sub_level")));
		msg.append(entry("/capitol team create <name> <tag> <color> [desc]", Component.translatable("commands.capitol.help.team.create")));
		msg.append(entry("/capitol team info", Component.translatable("commands.capitol.help.team.info")));
		msg.append(entry("/capitol team invite <player>", Component.translatable("commands.capitol.help.team.invite")));
		msg.append(entry("/capitol team kick <player>", Component.translatable("commands.capitol.help.team.kick")));
		msg.append(entry("/capitol team disband", Component.translatable("commands.capitol.help.team.disband")));
		msg.append(entry("/capitol team role edit <name> remove|rename|assign|permission", Component.translatable("commands.capitol.help.team.role.edit")));
		msg.append(entry("/capitol team role create <name>", Component.translatable("commands.capitol.help.team.role.create")));
		msg.append(entry("/capitol team protection <name>", Component.translatable("commands.capitol.help.team.protection")));
		msg.append(entry("/capitol team player <player> permission <perm>", Component.translatable("commands.capitol.help.team.player.permission")));
		msg.append(entry("/capitol team player <player> permission reset", Component.translatable("commands.capitol.help.team.player.permission.reset")));
		msg.append(entry("/capitol invites <team> accept|deny", Component.translatable("commands.capitol.help.invites"), false));

		context.getSource().sendSuccess(() -> msg, false);
		return 1;
	}

	private static MutableComponent entry(String command, MutableComponent description) {
		return entry(command, description, true);
	}

	private static MutableComponent entry(String command, MutableComponent description, boolean newline) {
		String newLine = newline ? "\n" : "";
		return Component.empty()
			.append(Component.literal(command).withStyle(ChatFormatting.AQUA).withStyle(s -> s.withBold(false)))
			.append(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY).withStyle(s -> s.withBold(false)))
			.append(description.withStyle(ChatFormatting.WHITE).withStyle(s -> s.withBold(false)))
			.append(Component.literal(newLine));
	}
}
