package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record C2SSetCapitolBlockMayor(BlockPos pos, UUID mayorUuid) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SSetCapitolBlockMayor> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_set_capitol_block_mayor"));

	public static final StreamCodec<ByteBuf, C2SSetCapitolBlockMayor> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		C2SSetCapitolBlockMayor::pos,
		ByteBufCodecs.STRING_UTF8,
		payload -> payload.mayorUuid().toString(),
		(pos, uuid) -> new C2SSetCapitolBlockMayor(pos, UUID.fromString(uuid))
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}