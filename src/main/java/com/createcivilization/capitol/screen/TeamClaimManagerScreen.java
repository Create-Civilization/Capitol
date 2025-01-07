package com.createcivilization.capitol.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.event.ClientEvents;
import com.createcivilization.capitol.packets.toserver.C2SInvitePlayer;
import com.createcivilization.capitol.team.Team;

import com.createcivilization.capitol.util.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamClaimManagerScreen extends Screen {

	private static final Component TITLE = Component.translatable("gui.capitol.claim_block_menu");
	private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Capitol.MOD_ID,  "textures/gui/capitol_block_screen.png");
	private static final Component EXITBUTTON_COMPONENT = Component.literal("X");
	private static final Component PLAYERS = Component.literal("Players");
	private static final Component INVITE = Component.literal("Invite");
	private static final Component PLAYERNAME = Component.literal("Player name");
	private static final Component CONFIRM = Component.literal("Confirm");

	private final Team menuTeam;
	private Component teamTitleComponent;
	private int teamTitleComponentWidth;
	private final int imageWidth, imageHeight;
	private int leftPos, topPos;

	private void toggleWidget(AbstractWidget widget, boolean to) {
		widget.active = to;
		widget.visible = to;
	}

	private void toggleWidgetList(List<AbstractWidget> widgets, boolean to) {
		widgets.forEach(widget -> toggleWidget(widget, to));
	}

	private void swapWidgetList(List<AbstractWidget> from, List<AbstractWidget> to) {
		toggleWidgetList(from, false);
		toggleWidgetList(to, true);
	}

	public TeamClaimManagerScreen(@NotNull Team team) {
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

		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;

		if (minecraft==null) return;

		List<AbstractWidget> mainScene = new ArrayList<>();
		List<AbstractWidget> playerScene = new ArrayList<>();

		EditBox inviteBox = addRenderableWidget(
			new EditBox(
				this.font,
				this.leftPos + 50,
				this.topPos + 141,
				50,
				15,
					PLAYERNAME
			)
		);
		toggleWidget(inviteBox, false);

		playerScene.add(inviteBox);

		playerScene.add(
			addRenderableWidget(
				Button.builder(
					INVITE,
					button -> {
						if (inviteBox.isActive()) {
							// They clicked to confirm
							String inviteName = inviteBox.getValue();
							for (Map.Entry<UUID, String> entry : ClientEvents.playerMap.entrySet())
								if (entry.getValue().equalsIgnoreCase(inviteName)) {
									PacketHandler.sendToServer(new C2SInvitePlayer(entry.getKey()));
									break;
							}
							toggleWidget(inviteBox, false);
							button.setMessage(INVITE);
						}else{
							// Open name prompt
							toggleWidget(inviteBox, true);
							button.setMessage(CONFIRM);
						}
					}
				)
					.bounds(
						this.leftPos + 50,
						this.topPos + 126,
						50,
						15
					)
					.build()
			)
		);

		mainScene.add(
			addRenderableWidget(
				Button.builder(
					PLAYERS,
					button -> {
						swapWidgetList(mainScene, playerScene);
						toggleWidget(inviteBox, false);
					}
				)
					.bounds(
						this.leftPos + 4,
							this.topPos + 16,
							50,
							15
					)
					.build()
			)
		);

		toggleWidgetList(playerScene, false);

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
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {

		this.renderBackground(guiGraphics);

		guiGraphics.blit(BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		super.render(guiGraphics, mouseX, mouseY, pPartialTick);

		guiGraphics.drawString(this.font, teamTitleComponent, (this.width - teamTitleComponentWidth) / 2, this.topPos + 2, 0x404040, false);

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