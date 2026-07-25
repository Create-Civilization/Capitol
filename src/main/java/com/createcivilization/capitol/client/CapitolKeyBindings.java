package com.createcivilization.capitol.client;

import com.createcivilization.capitol.Capitol;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = Capitol.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CapitolKeyBindings {

    private static final String CATEGORY = "key.categories.capitol";

    public static final KeyMapping toggleTeamChat = new KeyMapping(
        "key.capitol.toggle_team_chat",
        KeyConflictContext.IN_GAME,
        InputConstants.getKey(InputConstants.KEY_H, -1),
        CATEGORY
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(toggleTeamChat);
    }
}