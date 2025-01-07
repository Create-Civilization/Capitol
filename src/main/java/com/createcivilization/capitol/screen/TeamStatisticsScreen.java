package com.createcivilization.capitol.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.team.Team;

import com.createcivilization.capitol.util.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeamStatisticsScreen extends Screen {

	private static final Component TITLE = Component.translatable("gui." + Capitol.MOD_ID + ".claim_block_menu");
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Capitol.MOD_ID,  "textures/gui/capitol_block_screen.png");

	private final Team menuTeam;
	private Component teamTitleComponent;
	private int teamTitleComponentWidth;
	private final int imageWidth, imageHeight;
	private int leftPos, topPos;
	private int chunkAmount;
	private int playerAmount;
	private final Component EXIT = Component.literal("X");

	public TeamStatisticsScreen(@NotNull Team team) {
		super(TITLE);
		this.menuTeam = team;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	protected void init() {
		super.init();

		teamTitleComponent = Component.literal(menuTeam.getName());
		teamTitleComponentWidth = this.font.width(teamTitleComponent.getVisualOrderText());

		this.playerAmount = menuTeam.getPlayers().values().stream().mapToInt(List::size).sum();
		this.chunkAmount = menuTeam.getClaimedChunks().values().stream().mapToInt(List::size).sum();

		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;

		if (minecraft==null) return;

		addWidget(
			Button.builder(
					Component.empty(),
					button -> this.onClose()
				)
				.bounds((this.width + this.imageWidth) / 2 - this.font.width(EXIT.getVisualOrderText()) - 4,
					this.topPos + 4,
					9,
					9)
				.build()
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {

		this.renderBackground(guiGraphics);

		guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		super.render(guiGraphics, mouseX, mouseY, pPartialTick);

		guiGraphics.drawString(this.font, teamTitleComponent, (this.width - teamTitleComponentWidth) / 2, this.topPos + 2, 0x404040, false);

		guiGraphics.drawString(
			this.font,
			Component.literal("Amount of claimed chunks: " + chunkAmount + " / " + Config.maxChunks.getOrThrow()),
			this.leftPos + 6,
			this.topPos + 16,
			0xf2f2f2,
			false
		);

		guiGraphics.drawString(
			this.font,
			Component.literal("Member count: " + playerAmount + " / " + Config.maxMembers.getOrThrow()),
			this.leftPos + 4,
			this.topPos + 33,
			0xf2f2f2,
			false
		);

		guiGraphics.drawString(
			this.font,
			EXIT,
			(this.width + this.imageWidth) / 2 - this.font.width(EXIT.getVisualOrderText()) - 4,
			this.topPos + 4,
			0x787878,
			false
		);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}