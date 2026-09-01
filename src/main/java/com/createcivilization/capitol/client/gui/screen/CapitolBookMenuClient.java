package com.createcivilization.capitol.client.gui.screen;

import com.createcivilization.capitol.common.CapitolClientHooks;
import net.minecraft.client.Minecraft;

/**
 * Client-only hook assignment for {@link CapitolBookMenu}. {@link #register()}
 * is invoked from the dist-guarded client block in {@code Capitol}, so this
 * class is only ever loaded on the client distribution.
 */
public final class CapitolBookMenuClient {

	public static void register() {
		CapitolClientHooks.openCapitolBook = (team, capitolPos) ->
			Minecraft.getInstance().setScreen(new CapitolBookMenu(team, capitolPos));
	}

	private CapitolBookMenuClient() {}
}
