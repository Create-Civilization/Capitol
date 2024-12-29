package com.createcivilization.capitol.command.custom.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.*;
import net.minecraft.server.command.*;

/**
 * FUNNY HEHE :3
 */
public class SmiteCommand {

	public SmiteCommand() {}

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("smite")
			.requires((c) -> c.hasPermissionLevel(4))
			.then(CommandManager.argument("target", EntityArgumentType.entities())
				.executes(this::execute))
		);
	}

	public int execute(CommandContext<ServerCommandSource> command) throws CommandSyntaxException {
		var targets = EntityArgumentType.getEntities(command, "target");
		var level = command.getSource().getWorld();
		targets.forEach((target) -> {
			LightningEntity lightningBolt = new LightningEntity(EntityType.LIGHTNING_BOLT, level);
			lightningBolt.setCosmetic(true);
			lightningBolt.setPosition(target.getSteppingPos().toCenterPos());
			level.spawnEntity(lightningBolt);
		});
		return 1;
	}
}