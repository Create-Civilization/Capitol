package com.createcivilization.capitol.old.old;

import com.createcivilization.capitol.Capitol;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

import java.lang.reflect.Field;

@EventBusSubscriber(modid = Capitol.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {

	private KeyBindings() {}

	private static final String CATEGORY = "key.categories." + Capitol.MOD_ID;

	public static final KeyMapping openMenu = new KeyMapping(
		"key." + Capitol.MOD_ID + ".menu",
		KeyConflictContext.IN_GAME,
		InputConstants.getKey(
			InputConstants.KEY_M,
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
	public static void register(RegisterKeyMappingsEvent event) {
		for (Field field : KeyBindings.class.getDeclaredFields()) {
			if (field.getType() == KeyMapping.class) {
				try { event.register((KeyMapping) field.get(null)); } catch (IllegalAccessException ignored) {}
			}
		}
	}
}