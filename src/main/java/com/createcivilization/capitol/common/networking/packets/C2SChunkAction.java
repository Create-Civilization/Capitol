package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SChunkAction(long[] packedChunkPositions, boolean claim) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<C2SChunkAction> TYPE = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath("capitol", "c2s_chunk_action")
    );

    public static final StreamCodec<ByteBuf, C2SChunkAction> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public C2SChunkAction decode(ByteBuf buffer) {
            int len = ByteBufCodecs.VAR_INT.decode(buffer);
            long[] chunks = new long[len];
            for (int i = 0; i < len; i++) {
                chunks[i] = ByteBufCodecs.VAR_LONG.decode(buffer);
            }
            boolean claim = ByteBufCodecs.BOOL.decode(buffer);
            return new C2SChunkAction(chunks, claim);
        }

        @Override
        public void encode(ByteBuf buffer, C2SChunkAction value) {
            long[] chunks = value.packedChunkPositions();
            ByteBufCodecs.VAR_INT.encode(buffer, chunks.length);
            for (long chunk : chunks) {
                ByteBufCodecs.VAR_LONG.encode(buffer, chunk);
            }
            ByteBufCodecs.BOOL.encode(buffer, value.claim());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
