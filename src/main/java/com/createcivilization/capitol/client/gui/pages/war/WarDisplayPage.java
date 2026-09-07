package com.createcivilization.capitol.client.gui.pages.war;

import com.createcivilization.capitol.client.gui.base.Page;
import com.createcivilization.capitol.client.gui.screen.CapitolBookMenu;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.War;
import com.createcivilization.capitol.common.networking.packets.C2SEndWar;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

// migrated from the old WarDisplay page: shows both sides, their sizes, the
// defender's remaining chunks + capitols, and (for the declarer) an End War button
public class WarDisplayPage extends Page {

	public WarDisplayPage(War war, Team playerTeam) {
		super(Component.literal(war.declaringTeamName() + " vs " + war.receivingTeamName()));

		int totalParticipants = war.declaringSidePlayers() + war.receivingSidePlayers();
		CapitolBookMenu.addTableEntry(this, Component.literal("Total Participants:"), Component.literal(String.valueOf(totalParticipants)), 24);
		CapitolBookMenu.addTableEntry(this, Component.literal(war.declaringTeamName() + ":"), Component.literal(String.valueOf(war.declaringSidePlayers())), 38);
		CapitolBookMenu.addTableEntry(this, Component.literal(war.receivingTeamName() + ":"), Component.literal(String.valueOf(war.receivingSidePlayers())), 52);
		CapitolBookMenu.addTableEntry(this, Component.literal("Chunks left:"), Component.literal(String.valueOf(war.receivingTeamChunks())), 66);
		CapitolBookMenu.addTableEntry(this, Component.literal("Capitols left:"), Component.literal(String.valueOf(war.receivingTeamCapitols())), 80);

		if (war.isDeclarer(playerTeam.getId())) {
			addInteractable(new CapitolBookMenu.Button(13, 107, Component.literal("End War"), () -> {
				PacketDistributor.sendToServer(new C2SEndWar(war.declaringTeamId(), war.receivingTeamId()));
				Minecraft.getInstance().setScreen(null);
				Minecraft.getInstance().player.displayClientMessage(
					Component.literal("War successfully ended").withStyle(ChatFormatting.GREEN),
					true
				);
			}));
		}
	}
}