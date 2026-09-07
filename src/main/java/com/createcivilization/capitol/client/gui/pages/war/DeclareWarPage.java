package com.createcivilization.capitol.client.gui.pages.war;

import com.createcivilization.capitol.client.gui.base.Page;
import com.createcivilization.capitol.client.gui.screen.CapitolBookMenu;
import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.networking.packets.C2SDeclareWar;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

// migrated from the old DeclareWar page: type in a team name and declare war on it
public class DeclareWarPage extends Page {

	public DeclareWarPage(Team playerTeam) {
		super(Component.literal("Declare war"));

		// every team we know about from the claim borders, minus our own
		List<String> teamNames = ClientClaimCache.get().values().stream()
			.map(Team::getName)
			.distinct()
			.filter(name -> !name.equals(playerTeam.getName()))
			.toList();

		CapitolBookMenu.TextInput textInput = new CapitolBookMenu.TextInput(
			Component.literal("Team name"), 20, 24, teamNames
		);
		addInteractable(textInput);

		addInteractable(new CapitolBookMenu.Button(20, 47, Component.literal("Declare war"), () -> {
			String teamName = textInput.getValue();
			if (teamName == null || teamName.isBlank()) return;
			PacketDistributor.sendToServer(new C2SDeclareWar(teamName));
			Minecraft.getInstance().setScreen(null);
			Minecraft.getInstance().player.displayClientMessage(
				Component.literal("Successfully declared war on \"" + teamName + "\"").withStyle(ChatFormatting.GREEN),
				true
			);
		}));
	}
}