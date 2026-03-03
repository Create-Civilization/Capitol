package com.createcivilization.capitol.old.old.gui.screen;

import com.createcivilization.capitol.old.old.constants.ClientConstants;
import com.createcivilization.capitol.old.old.constants.CommonConstants;
import com.createcivilization.capitol.old.old.gui.base.BookScreen;
import com.createcivilization.capitol.old.old.gui.base.Interactable;
import com.createcivilization.capitol.old.old.payloads.bidirectional.add.BiAddTeam;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;

public class CreateTeamScreen extends BookScreen {

	public CreateTeamScreen() {
		super(Component.literal("Create OldTeam"));
	}

	@Override
	protected void init() {
		super.init();
		addInteractable(new Interactable.TextInteractable(title, this.leftPos + ClientConstants.INSTANCE.font.width(title.getString()) / 2 + 10, this.topPos + 14, null, null));
		BookMenu.TextInput input = new BookMenu.TextInput(Component.literal("OldTeam Name"), this.leftPos + 20, 60, null);
		BookMenu.TextInput color = new BookMenu.TextInput(Component.literal("OldTeam Color"), this.leftPos + 20, 83, CommonConstants.Colors.colors.keySet().stream().toList());
		addInteractable(new BookMenu.Button(this.rightPos + 20, 71, Component.literal("Create"), () -> {
			String value = input.getValue();
			if (TeamUtils.teamExists(value)) return;
			OldTeam created = OldTeam.TeamBuilder.create()
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
