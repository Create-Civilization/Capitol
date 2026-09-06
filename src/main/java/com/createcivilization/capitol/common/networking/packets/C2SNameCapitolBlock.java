package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record C2SNameCapitolBlock(BlockPos pos, String name, UUID teamId) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SNameCapitolBlock> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_name_capitol_block"));

	public static final StreamCodec<ByteBuf, C2SNameCapitolBlock> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		C2SNameCapitolBlock::pos,
		ByteBufCodecs.STRING_UTF8,
		C2SNameCapitolBlock::name,
		ByteBufCodecs.STRING_UTF8,
		payload -> payload.teamId().toString(),
		(pos, name, teamId) -> new C2SNameCapitolBlock(pos, name, UUID.fromString(teamId))
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}