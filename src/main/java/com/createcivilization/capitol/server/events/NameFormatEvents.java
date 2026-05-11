package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.modules.database.Database;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class NameFormatEvents {
	@SubscribeEvent
	public static void onNameFormat(PlayerEvent.NameFormat event) { // Mainly for chat messages
		Team team = DatabaseManager.database.getPlayerTeam(event.getEntity().getUUID());
		if (team == null) {
			return;
		}
		Component prevName = event.getDisplayname(); // the spelling mistake in getDisplayname instead of getDisplayName lmao
		Component tag = getTag(team);
		event.setDisplayname(Component.literal("").append(tag).append(" ").append(prevName));
	}

	@SubscribeEvent
	public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) { // Only for tab list
		Team team = DatabaseManager.database.getPlayerTeam(event.getEntity().getUUID());
		if (team == null) {
			return;
		}
		Component tag = getTag(team);
		event.setDisplayName(Component.literal("[").append(tag).append("] ").append(event.getEntity().getName()));
	}

	public static Component getTag(Team team) {
		return Component.literal(team.getTag()).withStyle(style -> style.withColor(TextColor.fromRgb(team.getColor().getRGB())));
	}
}
