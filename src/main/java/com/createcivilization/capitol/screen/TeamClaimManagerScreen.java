package com.createcivilization.capitol.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.packets.toserver.C2SInvitePlayer;
import com.createcivilization.capitol.util.*;

import net.minecraft.client.gui.components.*;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class TeamClaimManagerScreen extends GuiMenu {

	private static final Component TITLE = Component.translatable("gui.capitol.claim_block_menu");
	private static final Component PLAYERS = Component.literal("Players");
	private static final Component INVITE = Component.literal("Invite");
	private static final Component PLAYER_NAME = Component.literal("Player name");
	private static final Component CONFIRM = Component.literal("Confirm");

	public TeamClaimManagerScreen() {
		super(TITLE);
		this.imageWidth = 176;
		this.imageHeight = 166;
		this.backgroundTexture = new ResourceLocation(Capitol.MOD_ID,  "textures/gui/capitol_block_screen.png");
	}

	@Override
	protected void init() {
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;

		if (minecraft == null) return;

		Scene mainScene = new Scene();
		Scene invitePlayerScene = new Scene();

		super.init();

		EditBox inviteBox = invitePlayerScene.addRenderableWidget(
			new EditBox(
				this.font,
				this.leftPos + 50,
				this.topPos + 141,
				50,
				15,
				PLAYER_NAME
			)
		);

		invitePlayerScene.addRenderableWidget(
			Button.builder(
				INVITE,
				button -> {
					if (inviteBox.isActive()) {
						// They clicked to confirm
						String inviteName = inviteBox.getValue();
						for (Map.Entry<UUID, String> entry : ClientConstants.playerMap.entrySet())
							if (entry.getValue().equalsIgnoreCase(inviteName)) {
								PacketHandler.sendToServer(new C2SInvitePlayer(entry.getKey()));
								break;
						}
						invitePlayerScene.hideWidget(inviteBox);
						button.setMessage(INVITE);
					}else{
						// Open name prompt
						invitePlayerScene.showWidget(inviteBox);
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
		);

		mainScene.addRenderableWidget(
			Button.builder(
				PLAYERS,
				button -> {
					swapScene(mainScene, invitePlayerScene);
					invitePlayerScene.hideWidget(inviteBox);
				}
			)
				.bounds(
					this.leftPos + 4,
						this.topPos + 16,
						50,
						15
				)
				.build()
		);

		invitePlayerScene.hide();

		this.addScene(mainScene);
		this.addScene(invitePlayerScene);
	}
}