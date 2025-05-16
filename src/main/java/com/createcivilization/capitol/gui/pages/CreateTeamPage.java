package com.createcivilization.capitol.gui.pages;

import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.screen.BookMenu;
import net.minecraft.network.chat.Component;

public class CreateTeamPage extends Page {

	public CreateTeamPage() {
		super(Component.literal("Test"));
		addInteractableList(new BookMenu.Button(20,143, Component.literal("Create")));
		addInteractableList(new BookMenu.TextInput(Component.literal("test"), 20, 120));
	}
}
