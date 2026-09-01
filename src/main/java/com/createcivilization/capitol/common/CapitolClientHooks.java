package com.createcivilization.capitol.common;

import com.createcivilization.capitol.common.data.Team;
import net.minecraft.core.BlockPos;

// Lets common code open client-only screens without pulling client classes onto the server.
// Client code swaps in the real opener at startup; dedicated servers never touch it.
public final class CapitolClientHooks {

	@FunctionalInterface
	public interface CapitolBookOpener {
		void open(Team team, BlockPos capitolPos);
	}

	public static CapitolBookOpener openCapitolBook = (team, capitolPos) -> {};

	private CapitolClientHooks() {}
}
