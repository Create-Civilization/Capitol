package com.createcivilization.capitol.gui.pages.info;

import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.gui.screen.BookMenu;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;
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

		Team team = ClientConstants.getPlayerTeam().getOrThrow();
		newText.accept(Component.literal("Chunks:"), Component.literal(team.getAllChildChunks().size() + " / " + CapitolConfig.SERVER.maxChunks.get()), 14);
		newText.accept(Component.literal("Members:"), Component.literal(String.valueOf(team.getMembers().values().stream().mapToInt(List::size).sum())), 28);
		newText.accept(Component.literal("Wars:"), Component.literal(String.valueOf(TeamUtils.loadedWars.stream().filter(war -> war.getDeclaringTeam().idsMatch(war.getReceivingTeam())).toList().size())), 42);
	}
}
