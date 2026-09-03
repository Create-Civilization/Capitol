package com.createcivilization.capitol.client.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.client.CapitolKeyBindings;
import com.createcivilization.capitol.client.TeamChatState;
import com.createcivilization.capitol.client.renderer.BorderRenderer;
import com.createcivilization.capitol.common.networking.packets.C2SClaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2STeamChat;
import com.createcivilization.capitol.common.networking.packets.C2SUnclaimChunk;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.CLIENT)
public class ToggleEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Team Chat
        if (CapitolKeyBindings.toggleTeamChat.consumeClick()) {
            TeamChatState.teamChatEnabled = !TeamChatState.teamChatEnabled;

            if (TeamChatState.teamChatEnabled) {
                player.displayClientMessage(
                    Component.literal("Now talking in team chat").withStyle(ChatFormatting.GREEN),
                    true
                );
            } else {
                player.displayClientMessage(
                    Component.literal("Now talking in public chat").withStyle(ChatFormatting.GRAY),
                    true
                );
            }
        }

        // Claim chunk
        if (CapitolKeyBindings.claimChunk.consumeClick()) {
            PacketDistributor.sendToServer(new C2SClaimChunk(
                new long[]{ mc.player.chunkPosition().toLong() }
            ));
        }

        // Unclaim chunk
        if (CapitolKeyBindings.unclaimChunk.consumeClick()) {
            PacketDistributor.sendToServer(new C2SUnclaimChunk(
                new long[]{ mc.player.chunkPosition().toLong() }
            ));
        }

        // Toggle claim borders
        if (CapitolKeyBindings.toggleClaimBorders.consumeClick()) {
            BorderRenderer.showBorders = !BorderRenderer.showBorders;

            if (BorderRenderer.showBorders) {
                player.displayClientMessage(
                    Component.literal("Claim borders visible").withStyle(ChatFormatting.GREEN),
                    true
                );
            } else {
                player.displayClientMessage(
                    Component.literal("Claim borders hidden").withStyle(ChatFormatting.GRAY),
                    true
                );
            }
        }

    }

    @SubscribeEvent
    public static void onChat(ClientChatEvent event) {
        if (!TeamChatState.teamChatEnabled) return;

        event.setCanceled(true);
        PacketDistributor.sendToServer(new C2STeamChat(event.getMessage()));
    }
}