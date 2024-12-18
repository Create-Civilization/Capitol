package com.createcivilization.capitol.command.custom;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.*;

/**
 * FUNNY HEHE :3
 */
public class SmiteCommand {

	public SmiteCommand() {}

	public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("smite")
			.requires((c) -> c.hasPermission(4))
			.then(Commands.argument("target", EntityArgument.entities())
				.executes(this::execute))
		);
	}

	public int execute(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
		var targets = EntityArgument.getEntities(command, "target");
		var level = command.getSource().getLevel();
		targets.forEach((target) -> {
			LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
			lightningBolt.setVisualOnly(true);
			lightningBolt.setPos(target.getOnPos().getCenter());
			level.addFreshEntity(lightningBolt);
		});
		return 1;
	}
}