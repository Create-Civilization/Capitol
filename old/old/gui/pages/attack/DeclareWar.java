package com.createcivilization.capitol.old.old.gui.pages.attack;

import com.createcivilization.capitol.old.old.constants.ClientConstants;
import com.createcivilization.capitol.old.old.gui.base.Page;
import com.createcivilization.capitol.old.old.gui.base.SmartScreen;
import com.createcivilization.capitol.old.old.gui.screen.BookMenu;
import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.old.payloads.bidirectional.add.BiAddWar;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.util.data.DataManager;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import net.minecraft.network.chat.Component;

public class DeclareWar extends Page {
	public DeclareWar() {
		super(Component.literal("Declare war"));
	}

	@Override
	public void init(SmartScreen smartScreen) {
		BookMenu.TextInput textInput = new BookMenu.TextInput(Component.literal("OldTeam name"), 20, 24, DataManager.TeamData.LOADED_OLD_TEAMS.stream().filter(team -> !team.equals(ClientConstants.getPlayerTeam().getOrThrow())).map(OldTeam::getName).toList());
		addInteractable(textInput);
		addInteractable(new BookMenu.Button(20, 47, Component.literal("Declare war"), () -> {
			String teamName = textInput.getValue();
			PacketHandler.sendToServer(new BiAddWar(ClientConstants.getPlayerTeam().getOrThrow(), TeamUtils.getTeamByName(teamName).getOrThrow()));
			smartScreen.onClose();
			assert ClientConstants.INSTANCE.player != null;
			//TODO:: REJECT
			ClientConstants.INSTANCE.player.displayClientMessage(Component.literal("Successfully declared war on \"" + teamName + "\""), true);
		}));
		super.init(smartScreen);
	}
}
