package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.common.data.CapitolTier;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record S2COpenCapitolNamingScreen(BlockPos pos, UUID teamId, boolean isCapital,
										 @Nullable CapitolTier tier) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2COpenCapitolNamingScreen> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_open_capitol_naming_screen"));

	public static final StreamCodec<ByteBuf, S2COpenCapitolNamingScreen> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		S2COpenCapitolNamingScreen::pos,
		ByteBufCodecs.STRING_UTF8,
		payload -> payload.teamId().toString(),
		ByteBufCodecs.BOOL,
		S2COpenCapitolNamingScreen::isCapital,
		ByteBufCodecs.STRING_UTF8,
		payload -> payload.tier() == null ? "" : payload.tier().name(),
		(pos, teamId, isCapital, tierName) -> new S2COpenCapitolNamingScreen(pos, UUID.fromString(teamId), isCapital, CapitolTier.byName(tierName))
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}