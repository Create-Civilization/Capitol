package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.common.data.Team;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record S2COpenCapitolScreen(Team team, BlockPos capitolPos) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2COpenCapitolScreen> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_open_capitol_screen"));

	public static final StreamCodec<ByteBuf, S2COpenCapitolScreen> STREAM_CODEC = StreamCodec.composite(
		Team.STREAM_CODEC,
		S2COpenCapitolScreen::team,
		BlockPos.STREAM_CODEC,
		S2COpenCapitolScreen::capitolPos,
		S2COpenCapitolScreen::new
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
