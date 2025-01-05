package com.createcivilization.capitol.screen;

import com.createcivilization.capitol.Capitol;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class CreateRequestScreenTeam extends Screen {
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Capitol.MOD_ID,  "textures/gui/capitol_block_screen.png");

	private int teamTitleComponentWidth;
	private final int imageWidth, imageHeight;
	private int leftPos, topPos;

	private static final Component TITLE = Component.translatable("gui." + Capitol.MOD_ID + ".create_team");
	private static final Component EXITBUTTON_COMPONENT = Component.literal("X");
	private static final Component CREATETEAMBUTTON_COMPONENT = Component.literal("Create Team");
	private static final Component NOTEAM = Component.literal("You are not in a team, either create a team or join a team");
	private static final Component NAME_HERE = Component.literal("Team name here");
	private static final Component COLOR_HERE = Component.literal("Color name here");

	private static List<AbstractWidget> requestScene = new ArrayList<>();
	private static List<AbstractWidget> createScene = new ArrayList<>();

	private static void swapScene(List<AbstractWidget> fromScene, List<AbstractWidget> toScene) {
		for (AbstractWidget widget : fromScene) {
			widget.visible = false;
			widget.active = false;
		}
		for (AbstractWidget widget : toScene) {
			widget.active = true;
			widget.visible = true;
		}
	}

	public CreateRequestScreenTeam() {
		super(TITLE);
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	protected void init() {
		super.init();
		teamTitleComponentWidth = this.font.width(TITLE.getVisualOrderText());
		if (minecraft==null) return;

		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;

		// Close screen button
		addWidget(
			Button.builder(
					Component.empty(),
					button -> this.onClose()
				)
				.bounds((this.width + this.imageWidth) / 2 - this.font.width(EXITBUTTON_COMPONENT.getVisualOrderText()) - 4,
					this.topPos + 4,
					9,
					9)
				.build()
		);

		requestScene.add(addRenderableWidget(
			new StringWidget(
				this.leftPos + 6,
				this.topPos + 16,
				this.font.width(NOTEAM.getVisualOrderText()),
				18,
				NOTEAM,
				this.font
			)
		));

		requestScene.add(addRenderableWidget(
			Button.builder(
				CREATETEAMBUTTON_COMPONENT,
				button -> swapScene(requestScene, createScene)
			)
				.bounds(
					this.leftPos + 25,
					this.topPos + 34,
					126,
					18
				)
				.build()
		));

		createScene.add(
			addRenderableWidget(
				new EditBox(
					this.font,
					this.leftPos + 25,
					this.topPos + 34,
					126,
					18,
					NAME_HERE
				)
			)
		);

		createScene.add(
			addRenderableWidget(
				new EditBox(
					this.font,
					this.leftPos + 25,
					this.topPos + 60,
					126,
					18,
					COLOR_HERE
				)
			)
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {

		this.renderBackground(guiGraphics);

		guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		super.render(guiGraphics, mouseX, mouseY, pPartialTick);

		guiGraphics.drawString(this.font, TITLE, (this.width - teamTitleComponentWidth) / 2, this.topPos + 2, 0x404040, false);

		guiGraphics.drawString(
			this.font,
			EXITBUTTON_COMPONENT,
			(this.width + this.imageWidth) / 2 - this.font.width(EXITBUTTON_COMPONENT.getVisualOrderText()) - 4,
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
