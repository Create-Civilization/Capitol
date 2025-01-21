package com.createcivilization.capitol.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeamStatisticsScreen extends GuiMenu {

	private static final Component TITLE = Component.translatable("gui." + Capitol.MOD_ID + ".claim_block_menu");
	private static Component CHUNK_AMOUNT;
	private static Component MEMBER_COUNT;

	private final Team menuTeam;
	private int chunkAmount;
	private int playerAmount;

	public TeamStatisticsScreen(@NotNull Team team) {
		super(TITLE);
		this.menuTeam = team;
		this.imageWidth = 176;
		this.imageHeight = 166;
		this.backgroundTexture = new ResourceLocation(Capitol.MOD_ID,  "textures/gui/capitol_block_screen.png");
		CHUNK_AMOUNT = Component.literal("Amount of claimed chunks: " + chunkAmount + " / " + CapitolConfig.SERVER.maxChunks.get());
		MEMBER_COUNT = Component.literal("Member count: " + playerAmount + " / " + CapitolConfig.SERVER.maxMembers.get());
	}

	@Override
	protected void init() {

		this.playerAmount = menuTeam.getPlayers().values().stream().mapToInt(List::size).sum();
		this.chunkAmount = menuTeam.getClaimedChunks().values().stream().mapToInt(List::size).sum();

		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;

		if (minecraft == null) return;

		addRenderableWidget(
			new StringWidget(
				this.leftPos + 6,
				this.topPos + 16,
				this.font.width(CHUNK_AMOUNT.getVisualOrderText()),
				9,
				CHUNK_AMOUNT,
				this.font
			)
		);

		addRenderableWidget(
			new StringWidget(
				this.leftPos + 4,
				this.topPos + 33,
				this.font.width(MEMBER_COUNT.getVisualOrderText()),
				9,
				MEMBER_COUNT,
				this.font
			)
		);

		super.init();
	}
}