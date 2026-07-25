package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2STeamChat(String message) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2STeamChat> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath("capitol", "c2s_team_chat")
    );

    public static final StreamCodec<ByteBuf, C2STeamChat> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        C2STeamChat::message,
        C2STeamChat::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}