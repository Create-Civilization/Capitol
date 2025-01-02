package com.createcivilization.capitol;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {
	public static final KeyBindings INSTANCE = new KeyBindings();

	private KeyBindings() {}

	private static final String CATEGORY = "key.categories." + Capitol.MOD_ID;

	public final KeyMapping openStatistics = new KeyMapping(
		"key." + Capitol.MOD_ID + ".stats",
		KeyConflictContext.IN_GAME,
		InputConstants.getKey(
			InputConstants.KEY_L,
			-1
		),
		CATEGORY
	);

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		event.register(KeyBindings.INSTANCE.openStatistics);
	}
}
