package com.createcivilization.capitol.gui.pages.support;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.gui.screen.BookMenu;
import com.createcivilization.capitol.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.payloads.toserver.requests.C2SInvitePlayer;
import net.minecraft.network.chat.Component;

public class AddPlayer extends Page {
	public AddPlayer() {
		super(Component.literal("Invite"));
	}

	@Override
	public void init(SmartScreen smartScreen) {
		BookMenu.TextInput textInput = new BookMenu.TextInput(Component.literal("Player name"), 20, 24, ClientConstants.getPlayerList().stream().map(info -> info.getProfile().getName()).toList());
		addInteractable(textInput);
		addInteractable(new BookMenu.Button(20, 38, Component.literal("Invite Player"), () ->
			PacketHandler.sendToServer(new C2SInvitePlayer(textInput.getValue()))
		));

		super.init(smartScreen);
	}
}
