package com.createcivilization.capitol.common;

import com.createcivilization.capitol.common.data.Team;
import net.minecraft.core.BlockPos;

/**
 * Cross-distribution hook for opening client-only screens from common code.
 * <p>
 * Client-only code assigns {@link #openCapitolBook} at mod load. On a dedicated
 * server the no-op default is kept, so this class (and everything that
 * references it from common code) never touches client-only classes.
 */
public final class CapitolClientHooks {

	@FunctionalInterface
	public interface CapitolBookOpener {
		void open(Team team, BlockPos capitolPos);
	}

	public static CapitolBookOpener openCapitolBook = (team, capitolPos) -> {};

	private CapitolClientHooks() {}
}
