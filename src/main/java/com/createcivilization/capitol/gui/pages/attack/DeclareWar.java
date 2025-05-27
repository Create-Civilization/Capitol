package com.createcivilization.capitol.gui.pages.attack;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.gui.screen.BookMenu;
import com.createcivilization.capitol.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.payloads.bidirectional.add.BiAddWar;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.data.DataManager;
import com.createcivilization.capitol.util.team.TeamUtils;
import net.minecraft.network.chat.Component;

public class DeclareWar extends Page {
	public DeclareWar() {
		super(Component.literal("Declare war"));
	}

	@Override
	public void init(SmartScreen smartScreen) {
		BookMenu.TextInput textInput = new BookMenu.TextInput(Component.literal("Team name"), 20, 24, DataManager.TeamData.loadedTeams.stream().filter(team -> !team.equals(ClientConstants.getPlayerTeam().getOrThrow())).map(Team::getName).toList());
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
