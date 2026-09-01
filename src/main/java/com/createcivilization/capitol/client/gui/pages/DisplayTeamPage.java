package com.createcivilization.capitol.client.gui.pages;

import com.createcivilization.capitol.client.gui.base.Page;
import com.createcivilization.capitol.client.gui.screen.CapitolBookMenu;
import com.createcivilization.capitol.common.data.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class DisplayTeamPage extends Page {

	public DisplayTeamPage(Team team, BlockPos capitolPos) {
		super(Component.literal(team.getName()));

		CapitolBookMenu.addTableEntry(this, Component.literal("Tag:"), Component.literal(team.getTag()), 14);
		String claims = team.getMaxClaims() > 0
			? team.getCurrentClaims() + " / " + team.getMaxClaims()
			: String.valueOf(team.getCurrentClaims());
		CapitolBookMenu.addTableEntry(this, Component.literal("Chunks:"), Component.literal(claims), 28);
		CapitolBookMenu.addTableEntry(this, Component.literal("Capitol:"), Component.literal(formatCapitol(capitolPos)), 42);
		String description = team.getDescription() != null && !team.getDescription().isEmpty() ? team.getDescription() : "None";
		CapitolBookMenu.addTableEntry(this, Component.literal("Description:"), Component.literal(description), 56);
	}

	private static String formatCapitol(BlockPos pos) {
		if (pos == null) return "None";
		return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
	}
}
