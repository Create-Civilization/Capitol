package com.createcivilization.capitol.server;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import wiiu.mavity.wiiu_lib.util.ObjectHolder;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class ServerConstants {
	// The UUID of the server, acts like an identifier that the server runs a command.
	public static final UUID SERVER_UUID = new UUID(0L, 0L);
	public static final Function<CommandSourceStack, UUID> resolveUUIDFromSource =
		source -> source.isPlayer() ? Objects.requireNonNull(source.getPlayer()).getUUID() : SERVER_UUID;

	public static final ObjectHolder<MinecraftServer> SERVER = new ObjectHolder<>();
	public static final File CAPITOL_FOLDER = new File(System.getProperty("user.dir"), "capitol_data");
}
