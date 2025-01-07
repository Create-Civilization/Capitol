package com.createcivilization.capitol.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.toserver.C2SCreateTeam;
import com.createcivilization.capitol.util.PacketHandler;

import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class CreateTeam extends Screen {

	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Capitol.MOD_ID,  "textures/gui/capitol_block_screen.png");

	private int teamTitleComponentWidth;
	private final int imageWidth, imageHeight;
	private int leftPos, topPos;

	private static final Component TITLE = Component.translatable("gui." + Capitol.MOD_ID + ".create_team");
	private static final Component EXIT = Component.literal("X");
	private static final Component CREATE_TEAM = Component.literal("Create Team");
	private static final Component NO_TEAM = Component.literal("You are not in a team, either create a team or join a team");
	private static final Component NAME_HERE = Component.literal("Team name here");
	private static final Component COLOR_HERE = Component.literal("Color name here");
	private static final Component INVALID_COLOR = Component.literal("Invalid color");
	private static final Component TEAM_SUCCESS = Component.literal("Team successfully created");


	public CreateTeam() {
		super(TITLE);
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	protected void init() {
		super.init();
		teamTitleComponentWidth = this.font.width(TITLE.getVisualOrderText());
		if (minecraft == null || minecraft.player == null) return;

		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;

		// Close screen button
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

		StringWidget noTeam = addRenderableWidget(
			new StringWidget(
				this.leftPos + 6,
				this.topPos + 16,
				this.font.width(NO_TEAM.getVisualOrderText()),
				18,
				NO_TEAM,
				this.font
			)
		);

		EditBox teamName = addRenderableWidget(
			new EditBox(
				this.font,
				this.leftPos + 25,
				this.topPos + 34,
				126,
				18,
				NAME_HERE
			)
		);

		EditBox colorName = addRenderableWidget(
			new EditBox(
				this.font,
				this.leftPos + 25,
				this.topPos + 60,
				126,
				18,
				COLOR_HERE
			)
		);

		Button confirmCreation = addRenderableWidget(
			Button.builder(
					CREATE_TEAM,
				button -> {
					try {
						String value = teamName.getValue();
						if (TeamUtils.teamExists(value)) return;
						PacketHandler.sendToServer(new C2SCreateTeam(value, (Color)  Color.class.getField(colorName.getValue().toLowerCase()).get(null)));
					} catch (IllegalAccessException | NoSuchFieldException e) {
						this.onClose();
						minecraft.player.displayClientMessage(INVALID_COLOR, true);
						return;
					}
					// TODO: SUCCESS SCREEN
					minecraft.player.displayClientMessage(TEAM_SUCCESS, true);
					this.onClose();
				}
			)
				.bounds(
					this.leftPos + 25,
					this.topPos + 86,
					126,
					18
				)
				.build()
		);

		teamName.visible = false;
		teamName.active = false;
		colorName.visible = false;
		colorName.active = false;
		confirmCreation.active = false;
		confirmCreation.visible = false;

		addRenderableWidget(
			Button.builder(
					CREATE_TEAM,
				button -> {
					button.visible = false;
					button.active = false;
					noTeam.visible = false;
					noTeam.active = false;
					teamName.visible = true;
					teamName.active = true;
					colorName.visible = true;
					colorName.active = true;
					confirmCreation.active = true;
					confirmCreation.visible = true;
				}
			)
				.bounds(
					this.leftPos + 25,
					this.topPos + 34,
					126,
					18
				)
				.build()
		);

	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {

		this.renderBackground(guiGraphics);

		guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		super.render(guiGraphics, mouseX, mouseY, pPartialTick);

		guiGraphics.drawString(this.font, TITLE, (this.width - teamTitleComponentWidth) / 2, this.topPos + 2, 0x404040, false);

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