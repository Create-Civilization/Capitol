package com.createcivilization.capitol.old.old.gui.pages.info;

import com.createcivilization.capitol.old.old.config.CapitolConfig;
import com.createcivilization.capitol.old.old.constants.ClientConstants;
import com.createcivilization.capitol.old.old.gui.base.Page;
import com.createcivilization.capitol.old.old.gui.base.SmartScreen;
import com.createcivilization.capitol.old.old.gui.screen.BookMenu;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.util.data.DataManager;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.List;

public class DisplayTeam extends Page {

	public DisplayTeam() {
		super(Component.literal(ClientConstants.getPlayerTeam().getOrThrow().getName()));
	}

	@Override
	public void init(SmartScreen smartScreen) {
		super.init(smartScreen);

		// I love currying.
		TriConsumer<Component, Component, Integer> newText = (a,b,c) -> BookMenu.addTableEntry(this, a, b, c);

		OldTeam oldTeam = ClientConstants.getPlayerTeam().getOrThrow();
		newText.accept(Component.literal("Chunks:"), Component.literal(oldTeam.getAllChildChunks().size() + " / " + CapitolConfig.SERVER.maxChunks.get()), 14);
		newText.accept(Component.literal("Members:"), Component.literal(String.valueOf(oldTeam.getMembers().values().stream().mapToInt(List::size).sum())), 28);
		newText.accept(Component.literal("Wars:"), Component.literal(String.valueOf(DataManager.WarData.loadedWars.stream().filter(war -> war.getDeclaringTeam().idsMatch(war.getReceivingTeam())).toList().size())), 42);
	}
}
