package com.createcivilization.capitol.gui.pages.attack;

import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.gui.screen.BookMenu;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.team.War;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.util.TriConsumer;

public class WarDisplay extends Page {
	private final War war;

	public WarDisplay(War war) {
		super(Component.literal(war.toString()));
		this.war = war;
	}

	@Override
	public void init(SmartScreen smartScreen) {

		TriConsumer<Component, Component, Integer> newText = (a, b, c) -> BookMenu.addTableEntry(this, a, b, c);

		int declaringTeamAndAlliesTeamSize = war.getDeclaringTeamAndAlliesUUIDs().size();
		int receivingTeamAndAlliesTeamSize = this.war.getReceivingTeamAndAllies().size();
		newText.accept(Component.literal("Total Participants:"), Component.literal(String.valueOf(declaringTeamAndAlliesTeamSize + receivingTeamAndAlliesTeamSize)), 24);
		newText.accept(Component.literal(war.getDeclaringTeam().getName() + ":"), Component.literal(String.valueOf(declaringTeamAndAlliesTeamSize)), 38);
		newText.accept(Component.literal(war.getReceivingTeam().getName() + ":"), Component.literal(String.valueOf(receivingTeamAndAlliesTeamSize)), 52);
		Team updatedReceivingTeam = TeamUtils.getTeam(war.getReceivingTeam().getTeamId()).getOrThrow();
		newText.accept(Component.literal("Chunks left:"), Component.literal(String.valueOf(updatedReceivingTeam.getAllChildChunks().size())), 66);
		newText.accept(Component.literal("Capitols left:"), Component.literal(String.valueOf(updatedReceivingTeam.getDimensionDataMap().values().stream().mapToInt(tdd -> tdd.getCapitolDataList().size()).sum())), 80);

		super.init(smartScreen);
	}
}
