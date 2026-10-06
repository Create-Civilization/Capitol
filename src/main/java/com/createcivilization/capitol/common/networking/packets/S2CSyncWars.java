package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.common.data.War;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

// full war list, sent to a player on login and broadcast to everyone whenever
// a war is declared or ended
public record S2CSyncWars(List<War> wars) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CSyncWars> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("capitol", "s2c_sync_wars"));

	public static final StreamCodec<ByteBuf, S2CSyncWars> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public S2CSyncWars decode(ByteBuf buffer) {
			List<War> wars = ByteBufCodecs.collection(ArrayList::new, War.STREAM_CODEC).decode(buffer);
			return new S2CSyncWars(wars);
		}

		@Override
		public void encode(ByteBuf buffer, S2CSyncWars value) {
			ByteBufCodecs.collection(ArrayList::new, War.STREAM_CODEC).encode(buffer, new ArrayList<>(value.wars()));
		}
	};

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}