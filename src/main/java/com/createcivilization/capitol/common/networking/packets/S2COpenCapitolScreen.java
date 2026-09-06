package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.data.Team;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record S2COpenCapitolScreen(Team team, BlockPos capitolPos, boolean isCapital,
								   @Nullable CapitolTier tier, boolean canUpgrade) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2COpenCapitolScreen> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_open_capitol_screen"));

	public static final StreamCodec<ByteBuf, S2COpenCapitolScreen> STREAM_CODEC = StreamCodec.composite(
		Team.STREAM_CODEC,
		S2COpenCapitolScreen::team,
		BlockPos.STREAM_CODEC,
		S2COpenCapitolScreen::capitolPos,
		ByteBufCodecs.BOOL,
		S2COpenCapitolScreen::isCapital,
		ByteBufCodecs.STRING_UTF8,
		payload -> payload.tier() == null ? "" : payload.tier().name(),
		ByteBufCodecs.BOOL,
		S2COpenCapitolScreen::canUpgrade,
		(team, pos, isCapital, tierName, canUpgrade) -> new S2COpenCapitolScreen(
			team, pos, isCapital, CapitolTier.byName(tierName), canUpgrade)
	);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
