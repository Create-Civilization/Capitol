package com.createcivilization.capitol.gui.pages.attack;

import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.gui.screen.BookMenu;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.network.chat.Component;

public class DeclareWar extends Page {
	public DeclareWar() {
		super(Component.literal("Declare war"));
		addInteractableList(new BookMenu.TextInput(Component.literal("Team name"), 20, 24, TeamUtils.loadedTeams.stream().map(Team::getName).toList()));
	}

	@Override
	public void init(SmartScreen smartScreen) {
		super.init(smartScreen);
	}
}
