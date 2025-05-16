package com.createcivilization.capitol.gui.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.constants.CommonConstants;
import com.createcivilization.capitol.gui.base.Asset;
import com.createcivilization.capitol.gui.base.Interactable;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.packets.bidirectional.BiAddTeam;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.PacketHandler;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;

public class CreateTeamScreen extends SmartScreen {

	private static final Asset ASSET = new Asset(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "textures/gui/book_gui.png"), 421, 212);
	private static final Asset.Blit BACKGROUND = ASSET.blit(0, 0, 295, 179);

	int leftPos, topPos, rightPos;

	public CreateTeamScreen() {
		super(Component.literal("Create Team"));
	}

	@Override
	protected void init() {
		int halfWidth = BACKGROUND.getBlitWidth() / 2;
		this.leftPos = (this.width / 2) - halfWidth;
		this.rightPos = leftPos + halfWidth;
		this.topPos = (this.height - BACKGROUND.getBlitHeight()) / 2;
		Capitol.LOGGER.info("{} {} {} {}", halfWidth, leftPos, rightPos, topPos);
		addInteractable(new Interactable.TextInteractable(title, this.leftPos + ClientConstants.INSTANCE.font.width(title.getString()) / 2 + 10, this.topPos + 14, null, null));
		BookMenu.TextInput input = new BookMenu.TextInput(Component.literal("Team Name"), this.leftPos + 20, 60);
		BookMenu.TextInput color = new BookMenu.TextInput(Component.literal("Team Color"), this.leftPos + 20, 83);
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

	@Override
	protected Asset.Blit getBackgroundBlit() {
		return BACKGROUND;
	}
}
