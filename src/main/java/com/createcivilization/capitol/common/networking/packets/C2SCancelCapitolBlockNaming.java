package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record C2SCancelCapitolBlockNaming(BlockPos pos, UUID teamId) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SCancelCapitolBlockNaming> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_cancel_capitol_block_naming"));

	public static final StreamCodec<ByteBuf, C2SCancelCapitolBlockNaming> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		C2SCancelCapitolBlockNaming::pos,
		ByteBufCodecs.STRING_UTF8,
		payload -> payload.teamId().toString(),
		(pos, teamId) -> new C2SCancelCapitolBlockNaming(pos, UUID.fromString(teamId))
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}