package com.createcivilization.capitol;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Capitol.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

	private KeyBindings() {}

	private static final String CATEGORY = "key.categories." + Capitol.MOD_ID;

	public static final KeyMapping openStatistics = new KeyMapping(
		"key." + Capitol.MOD_ID + ".stats",
		KeyConflictContext.IN_GAME,
		InputConstants.getKey(
			InputConstants.KEY_L,
			-1
		),
		CATEGORY
	);

	public static final KeyMapping viewChunks = new KeyMapping(
		"key." + Capitol.MOD_ID + ".view_chunks",
		KeyConflictContext.IN_GAME,
		InputConstants.getKey(
			InputConstants.KEY_V,
			-1
		),
		CATEGORY
	);

	public static final KeyMapping openClaimMenu = new KeyMapping(
		"key." + Capitol.MOD_ID + ".open_claim_menu",
		KeyConflictContext.IN_GAME,
		InputConstants.getKey(
			InputConstants.KEY_M,
			-1
		),
		CATEGORY
	);

	public static final KeyMapping toggleTeamChat = new KeyMapping(
		"key." + Capitol.MOD_ID + ".toggle_team_chat",
		KeyConflictContext.IN_GAME,
		InputConstants.getKey(
			InputConstants.KEY_H,
			-1
		),
		CATEGORY
	);

	public static final KeyMapping claim_chunk = new KeyMapping(
		"key." + Capitol.MOD_ID + ".claim_chunk",
		KeyConflictContext.IN_GAME,
		InputConstants.UNKNOWN,
		CATEGORY
	);

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) throws IllegalAccessException {
		for (Field field : KeyBindings.class.getDeclaredFields()) {
			if (field.getType() == KeyMapping.class) event.register((KeyMapping) field.get(null));
		}
	}
}