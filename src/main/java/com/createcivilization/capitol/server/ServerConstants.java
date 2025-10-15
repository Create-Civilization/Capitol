package com.createcivilization.capitol.server;

import net.minecraft.commands.CommandSourceStack;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class ServerConstants {
	// The UUID of the server, acts like an identifier that the server runs a command.
	public static final UUID SERVER_UUID = new UUID(0L, 0L);
	public static final Function<CommandSourceStack, UUID> resolveUUIDFromSource =
		source -> source.isPlayer() ? Objects.requireNonNull(source.getPlayer()).getUUID() : SERVER_UUID;
}
