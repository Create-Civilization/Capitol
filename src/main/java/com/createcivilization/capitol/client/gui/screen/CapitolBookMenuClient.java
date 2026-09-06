package com.createcivilization.capitol.client.gui.screen;

import com.createcivilization.capitol.common.CapitolClientHooks;
import net.minecraft.client.Minecraft;

// Swaps in the real capitol book opener. Only ever loaded on the client.
public final class CapitolBookMenuClient {

	public static void register() {
		CapitolClientHooks.openCapitolBook = (team, capitolPos, isCapital, tier, canUpgrade) ->
			Minecraft.getInstance().setScreen(new CapitolBookMenu(team, capitolPos, isCapital, tier, canUpgrade));
	}

	private CapitolBookMenuClient() {}
}
