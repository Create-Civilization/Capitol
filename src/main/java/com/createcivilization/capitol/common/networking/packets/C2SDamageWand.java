package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.Capitol;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SDamageWand() implements CustomPacketPayload {
    public static final Type<C2SDamageWand> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "c2s_damage_wand"));
    public static final StreamCodec<ByteBuf, C2SDamageWand> STREAM_CODEC =
        StreamCodec.unit(new C2SDamageWand());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}