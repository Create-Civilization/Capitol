package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.common.data.CapitolMember;
import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.data.Team;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record S2COpenCapitolScreen(Team team, BlockPos capitolPos, boolean isCapital,
								   @Nullable CapitolTier tier, boolean canUpgrade,
								   @Nullable String blockName, @Nullable String mayorName,
								   List<CapitolMember> members, boolean canAssignMayor) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2COpenCapitolScreen> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_open_capitol_screen"));

	// too many fields for composite(), so hand-roll the codec
	public static final StreamCodec<ByteBuf, S2COpenCapitolScreen> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public S2COpenCapitolScreen decode(ByteBuf buffer) {
			Team team = Team.STREAM_CODEC.decode(buffer);
			BlockPos pos = BlockPos.STREAM_CODEC.decode(buffer);
			boolean isCapital = ByteBufCodecs.BOOL.decode(buffer);
			String tierName = ByteBufCodecs.STRING_UTF8.decode(buffer);
			boolean canUpgrade = ByteBufCodecs.BOOL.decode(buffer);
			String blockName = ByteBufCodecs.STRING_UTF8.decode(buffer);
			String mayorName = ByteBufCodecs.STRING_UTF8.decode(buffer);
			List<CapitolMember> members = ByteBufCodecs.collection(ArrayList::new, CapitolMember.STREAM_CODEC).decode(buffer);
			boolean canAssignMayor = ByteBufCodecs.BOOL.decode(buffer);
			return new S2COpenCapitolScreen(
				team, pos, isCapital, CapitolTier.byName(tierName), canUpgrade,
				blockName.isEmpty() ? null : blockName,
				mayorName.isEmpty() ? null : mayorName,
				members, canAssignMayor
			);
		}

		@Override
		public void encode(ByteBuf buffer, S2COpenCapitolScreen payload) {
			Team.STREAM_CODEC.encode(buffer, payload.team());
			BlockPos.STREAM_CODEC.encode(buffer, payload.capitolPos());
			ByteBufCodecs.BOOL.encode(buffer, payload.isCapital());
			ByteBufCodecs.STRING_UTF8.encode(buffer, payload.tier() == null ? "" : payload.tier().name());
			ByteBufCodecs.BOOL.encode(buffer, payload.canUpgrade());
			ByteBufCodecs.STRING_UTF8.encode(buffer, payload.blockName() == null ? "" : payload.blockName());
			ByteBufCodecs.STRING_UTF8.encode(buffer, payload.mayorName() == null ? "" : payload.mayorName());
			ByteBufCodecs.collection(ArrayList::new, CapitolMember.STREAM_CODEC).encode(buffer, new ArrayList<>(payload.members()));
			ByteBufCodecs.BOOL.encode(buffer, payload.canAssignMayor());
		}
	};

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}