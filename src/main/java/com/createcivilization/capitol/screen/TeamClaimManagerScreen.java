package com.createcivilization.capitol.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.packets.toserver.C2SInvitePlayer;
import com.createcivilization.capitol.util.*;

import net.minecraft.client.gui.components.*;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import wiiu.mavity.wiiu_lib.util.network.NetworkUtil;

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
		Scene playerScene = new Scene();

		super.init();

		int i = 0;
		for(Map.Entry<String, List<UUID>> entry : ClientConstants.playerTeam.getOrThrow().getMembers().entrySet()) {
			Component roleComponent = Component.literal(entry.getKey());
			playerScene.addRenderableWidget(
				new StringWidget(
					this.leftPos + 10,
					this.topPos + 10 + ( i++ * 20 ),
					this.font.width(roleComponent.getVisualOrderText()),
					9,
					roleComponent,
					this.font
				)
			);
			for (UUID uuid : entry.getValue()) {
				if (CapitolConfig.SERVER.offlineMode.get()) uuid = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5");
				Component playerNameComponent = Component.literal(NetworkUtil.getUsernameFromUUID(uuid));
				playerScene.addRenderableWidget(
					Button.builder(
						playerNameComponent,
						button -> {}
					).bounds(
						this.leftPos + 10,
						this.topPos + 10 + ( i++ * 20 ),
						this.font.width(playerNameComponent.getVisualOrderText()),
						9
					).build()
				);
			}
		}

		EditBox inviteBox = playerScene.addRenderableWidget(
			new EditBox(
				this.font,
				this.leftPos + 50,
				this.topPos + 141,
				50,
				15,
				PLAYER_NAME
			)
		);

		playerScene.addRenderableWidget(
			Button.builder(
				INVITE,
				button -> {
					if (inviteBox.isActive()) {
						// They clicked to confirm
						String inviteName = inviteBox.getValue();
						PacketHandler.sendToServer(new C2SInvitePlayer(inviteName));
						playerScene.hideWidget(inviteBox);
						button.setMessage(INVITE);
					}else{
						// Open name prompt
						playerScene.showWidget(inviteBox);
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
					swapScene(mainScene, playerScene);
					playerScene.hideWidget(inviteBox);
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

		playerScene.hide();

		this.addScene(mainScene);
		this.addScene(playerScene);
	}
}