package com.createcivilization.capitol.client;

import com.createcivilization.capitol.Capitol;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.CLIENT)
public class CapitolKeyBindings {

    private static final String CATEGORY = "key.categories.capitol";
    private static final InputConstants.Key UNBOUND = InputConstants.UNKNOWN;

    private static KeyMapping binding(String translationKey) {
        return new KeyMapping(
            translationKey,
            KeyConflictContext.IN_GAME,
            UNBOUND,
            CATEGORY
        );
    }

    public static final KeyMapping toggleTeamChat = binding("key.capitol.toggle_team_chat");
    public static final KeyMapping claimChunk = binding("key.capitol.claim_chunk");
    public static final KeyMapping unclaimChunk = binding("key.capitol.unclaim_chunk");
    public static final KeyMapping toggleClaimBorders = binding("key.capitol.toggle_claim_borders");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(toggleTeamChat);
        event.register(claimChunk);
        event.register(unclaimChunk);
        event.register(toggleClaimBorders);
    }
}