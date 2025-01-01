package com.createcivilization.capitol.event;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if (KeyBindings.INSTANCE.exampleKey.consumeClick()) {
			Minecraft instance = Minecraft.getInstance();
			assert instance.player != null;
			instance.player.sendSystemMessage(Component.literal("Working"));
		}
	}
}
