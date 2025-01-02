package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.KeyBindings;
import com.createcivilization.capitol.screen.TeamStatisticsScreen;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wiiu.mavity.util.ObjectHolder;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
	private static final Component NOT_IN_TEAM = Component.literal("You are not in a team");


	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if (KeyBindings.INSTANCE.openStatistics.consumeClick()) {
			Minecraft instance = Minecraft.getInstance();
			Player player = instance.player;
			if (player == null) return;
			ObjectHolder<Team> team = TeamUtils.getTeam(player);
			if (team.isEmpty()) {
				player.displayClientMessage(NOT_IN_TEAM, true);
				return;
			}
			instance.setScreen(new TeamStatisticsScreen(team.getOrThrow()));
		}
	}
}
