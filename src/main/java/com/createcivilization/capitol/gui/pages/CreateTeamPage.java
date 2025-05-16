package com.createcivilization.capitol.gui.pages;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.constants.CommonConstants;
import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.screen.BookMenu;
import com.createcivilization.capitol.packets.bidirectional.BiAddTeam;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.PacketHandler;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;

public class CreateTeamPage extends Page {

	public CreateTeamPage() {
		super(Component.literal("Test"));
		BookMenu.TextInput input = new BookMenu.TextInput(Component.literal("test"), 20, 120);
		addInteractableList(new BookMenu.Button(20,143, Component.literal("Create"), () -> {
			String value = input.getValue();
			if (TeamUtils.teamExists(value)) return;
			Team created = Team.TeamBuilder.create()
				.setName(value)
				.setTeamId(TeamUtils.createRandomTeamId())
				.addPlayer("owner", new ArrayList<>())
				.setColor(CommonConstants.Colors.colors.getOrDefault("white", Color.BLACK))
				.build();
			PacketHandler.sendToServer(new BiAddTeam(created));
			// TODO: SUCCESS SCREEN
			ClientConstants.INSTANCE.player.displayClientMessage(Component.literal("Team successfully created"), true);
		}));
		addInteractableList(input);
	}
}
