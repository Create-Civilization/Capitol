package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamRole;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class NameFormatEvents {
	@SubscribeEvent
	public static void onNameFormat(PlayerEvent.NameFormat event) { // Mainly for chat messages
		if (!CapitolConfig.DISPLAY_TAGS_IN_CHAT.get()) {
			return;
		}

		Team team = DatabaseManager.database.getPlayerTeam(event.getEntity().getUUID());
		if (team == null) {
			return;
		}
		TeamRole role = DatabaseManager.database.getPlayerRole(event.getEntity(), team);

		Component prevName = event.getDisplayname();
		Component tag = getTag(team, role);
		// This is so we can easily preserve the previous name and team tag's styles
		String formatted = CapitolConfig.TEAM_TAG_FORMAT.get().replaceAll("%t", "%1$s").replaceAll("%p", "%2$s");
		event.setDisplayname(Component.translatable(formatted, tag, prevName));
	}

	@SubscribeEvent
	public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) { // Only for tab list
		if (!CapitolConfig.DISPLAY_TAGS_IN_TAB_LIST.get()) {
			return;
		}

		Team team = DatabaseManager.database.getPlayerTeam(event.getEntity().getUUID());
		if (team == null) {
			return;
		}
		TeamRole role = DatabaseManager.database.getPlayerRole(event.getEntity(), team);

		Component tag = getTag(team, role);
		String formatted = CapitolConfig.TEAM_TAG_FORMAT.get().replaceAll("%t", "%1$s").replaceAll("%p", "%2$s");
		event.setDisplayName(Component.translatable(formatted, tag, event.getDisplayName()));
	}

	public static Component getTag(Team team, TeamRole role) {
		TextColor color;

		if (role.color() != null) {
			color = TextColor.fromRgb(role.color().getRGB());
		} else {
			color = TextColor.fromRgb(team.getColor().getRGB());
		}

		return Component.literal(team.getTag()).withStyle(style -> style.withColor(color));
	}
}
