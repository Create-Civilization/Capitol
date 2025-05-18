package com.createcivilization.capitol.gui.screen;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.constants.CommonConstants;
import com.createcivilization.capitol.gui.base.BookScreen;
import com.createcivilization.capitol.gui.base.Interactable;
import com.createcivilization.capitol.packets.bidirectional.add.BiAddTeam;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.packets.bidirectional.PacketHandler;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;

public class CreateTeamScreen extends BookScreen {

	public CreateTeamScreen() {
		super(Component.literal("Create Team"));
	}

	@Override
	protected void init() {
		super.init();
		addInteractable(new Interactable.TextInteractable(title, this.leftPos + ClientConstants.INSTANCE.font.width(title.getString()) / 2 + 10, this.topPos + 14, null, null));
		BookMenu.TextInput input = new BookMenu.TextInput(Component.literal("Team Name"), this.leftPos + 20, 60, null);
		BookMenu.TextInput color = new BookMenu.TextInput(Component.literal("Team Color"), this.leftPos + 20, 83, null);
		addInteractable(new BookMenu.Button(this.rightPos + 20, 71, Component.literal("Create"), () -> {
			String value = input.getValue();
			if (TeamUtils.teamExists(value)) return;
			Team created = Team.TeamBuilder.create()
				.setName(value)
				.setTeamId(TeamUtils.createRandomTeamId())
				.addPlayer("owner", new ArrayList<>())
				.setColor(CommonConstants.Colors.colors.getOrDefault(color.getValue(), Color.BLACK))
				.build();
			PacketHandler.sendToServer(new BiAddTeam(created));
			// TODO: SUCCESS SCREEN
			this.onClose();
			ClientConstants.INSTANCE.player.displayClientMessage(ClientConstants.TEAM_SUCCESSFULLY_CREATED, true);
		}));
		addInteractable(input);
		addInteractable(color);
		super.init();
	}
}
