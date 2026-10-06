package com.createcivilization.capitol.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SUpgradeCapitolBlock(BlockPos pos) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<C2SUpgradeCapitolBlock> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "c2s_upgrade_capitol_block"));

	public static final StreamCodec<ByteBuf, C2SUpgradeCapitolBlock> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		C2SUpgradeCapitolBlock::pos,
		C2SUpgradeCapitolBlock::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
