package com.createcivilization.capitol.common;

import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolNamingScreen;
import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolScreen;

// Lets common code open client-only screens without pulling client classes onto the server.
// Client code swaps in the real opener at startup; dedicated servers never touch it.
public final class CapitolClientHooks {

	@FunctionalInterface
	public interface CapitolBookOpener {
		void open(S2COpenCapitolScreen payload);
	}

	@FunctionalInterface
	public interface CapitolNamingOpener {
		void open(S2COpenCapitolNamingScreen payload);
	}

	public static CapitolBookOpener openCapitolBook = payload -> {};
	public static CapitolNamingOpener openCapitolNaming = payload -> {};

	private CapitolClientHooks() {}
}