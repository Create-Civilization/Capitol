package com.createcivilization.capitol.common.networking.packets;

import com.createcivilization.capitol.Capitol;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record S2CSubClaimRemove(UUID id) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<S2CSubClaimRemove> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "s2c_sub_claim_remove"));

	public static final StreamCodec<ByteBuf, S2CSubClaimRemove> STREAM_CODEC = new StreamCodec<>() {
		@Override
		public S2CSubClaimRemove decode(ByteBuf buffer) {
			return new S2CSubClaimRemove(UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(buffer)));
		}

		@Override
		public void encode(ByteBuf buffer, S2CSubClaimRemove value) {
			ByteBufCodecs.STRING_UTF8.encode(buffer, value.id().toString());
		}
	};

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}