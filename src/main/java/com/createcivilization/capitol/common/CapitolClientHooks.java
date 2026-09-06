package com.createcivilization.capitol.common;

import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.data.Team;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

// Lets common code open client-only screens without pulling client classes onto the server.
// Client code swaps in the real opener at startup; dedicated servers never touch it.
public final class CapitolClientHooks {

	@FunctionalInterface
	public interface CapitolBookOpener {
		void open(Team team, BlockPos capitolPos, boolean isCapital, @Nullable CapitolTier tier, boolean canUpgrade);
	}

	public static CapitolBookOpener openCapitolBook = (team, capitolPos, isCapital, tier, canUpgrade) -> {};

	private CapitolClientHooks() {}
}
