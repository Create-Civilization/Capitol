package com.createcivilization.capitol.gui.pages.info;

import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.team.Team;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BiFunction;

public class DisplayTeam extends Page {

	public DisplayTeam() {
		super(ClientConstants.TEAM_STATISTICS);
	}

	@Override
	public void init(SmartScreen smartScreen) {
		super.init(smartScreen);

		BiFunction<Component, Integer, TextInteractable> newText = (title, y) -> new TextInteractable(title, 72 - ClientConstants.INSTANCE.font.width(title.getString()) / 2, 14 + y, null, null);

		Team team = ClientConstants.getPlayerTeam().getOrThrow();
		addInteractableList(newText.apply(Component.literal("Claimed chunks:"), 10));
		addInteractableList(newText.apply(Component.literal(team.getAllChildChunks().size() + " / " + CapitolConfig.SERVER.maxChunks.get()), 20));
		addInteractableList(newText.apply(Component.literal("Members:"), 30));
		addInteractableList(newText.apply(Component.literal(String.valueOf(team.getMembers().values().stream().mapToInt(List::size).sum())), 40));
	}
}
