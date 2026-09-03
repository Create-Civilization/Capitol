package com.createcivilization.capitol.client;

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
	private static final InputConstants.Key UNBOUND = InputConstants.getKey(InputConstants.KEY_UNKNOWN, -1);

	private static KeyMapping binding(String translationKey) {
		return new KeyMapping(
			translationKey,
			KeyConflictContext.IN_GAME,
			UNBOUND,
			CATEGORY
		);
	}

	public static final KeyMapping openMenu = binding("key." + Capitol.MOD_ID + ".menu");
	public static final KeyMapping claimChunk = binding("key.capitol.claim_chunk");
	public static final KeyMapping unclaimChunk = binding("key.capitol.unclaim_chunk");
	public static final KeyMapping toggleClaimBorders = binding("key.capitol.toggle_claim_borders");
	public static final KeyMapping viewChunks = binding("key." + Capitol.MOD_ID + ".view_chunks");
	public static final KeyMapping toggleTeamChat = binding("key." + Capitol.MOD_ID + ".toggle_team_chat");
	public static final KeyMapping claim_chunk = binding("key." + Capitol.MOD_ID + ".claim_chunk");

	@SubscribeEvent
	public static void register(RegisterKeyMappingsEvent event) {
		for (Field field : KeyBindings.class.getDeclaredFields()) {
			if (field.getType() == KeyMapping.class) {
				try { event.register((KeyMapping) field.get(null)); } catch (IllegalAccessException ignored) {}
			}
		}
	}
}