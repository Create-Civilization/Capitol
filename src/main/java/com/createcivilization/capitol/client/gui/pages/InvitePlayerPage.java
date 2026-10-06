package com.createcivilization.capitol.client.gui.pages;

import com.createcivilization.capitol.client.gui.base.Page;
import com.createcivilization.capitol.client.gui.base.SmartScreen;
import com.createcivilization.capitol.client.gui.screen.CapitolBookMenu;
import com.createcivilization.capitol.common.networking.packets.C2SInvitePlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class InvitePlayerPage extends Page {

	public InvitePlayerPage() {
		super(Component.literal("Invite"));
	}

	@Override
	public void init(SmartScreen smartScreen) {
		List<String> playerNames = Minecraft.getInstance().getConnection() != null
			? Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
				.map(info -> info.getProfile().getName())
				.toList()
			: List.of();

		CapitolBookMenu.TextInput textInput = new CapitolBookMenu.TextInput(Component.literal("Player name"), 20, 24, playerNames);
		addInteractable(textInput);
		addInteractable(new CapitolBookMenu.Button(20, 38, Component.literal("Invite Player"), () ->
			PacketDistributor.sendToServer(new C2SInvitePlayer(textInput.getValue()))
		));

		super.init(smartScreen);
	}
}
