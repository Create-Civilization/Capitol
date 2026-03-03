package com.createcivilization.capitol.old.old.gui.pages.attack;

import com.createcivilization.capitol.old.old.constants.ClientConstants;
import com.createcivilization.capitol.old.old.gui.base.Page;
import com.createcivilization.capitol.old.old.gui.base.SmartScreen;
import com.createcivilization.capitol.old.old.gui.screen.BookMenu;
import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.old.payloads.bidirectional.remove.BiRemoveWar;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.team.War;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
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
		OldTeam updatedReceivingOldTeam = TeamUtils.getTeam(war.getReceivingTeam().getTeamId()).getOrThrow();
		newText.accept(Component.literal("Chunks left:"), Component.literal(String.valueOf(updatedReceivingOldTeam.getAllChildChunks().size())), 66);
		newText.accept(Component.literal("Capitols left:"), Component.literal(String.valueOf(updatedReceivingOldTeam.getDimensionDataMap().values().stream().mapToInt(tdd -> tdd.getCapitolDataList().size()).sum())), 80);

		if (ClientConstants.getPlayerTeam().getOrThrow().idsMatch(war.getDeclaringTeam())) addInteractable(new BookMenu.Button(13, 107, Component.literal("End War"), () -> {
			PacketHandler.sendToServer(new BiRemoveWar(war));
			smartScreen.onClose();
			assert ClientConstants.INSTANCE.player != null;
			ClientConstants.INSTANCE.player.displayClientMessage(Component.literal("War successfully ended"), true);
		}));

		super.init(smartScreen);
	}
}
