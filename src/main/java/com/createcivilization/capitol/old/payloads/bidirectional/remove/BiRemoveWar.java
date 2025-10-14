package com.createcivilization.capitol.old.payloads.bidirectional.remove;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.payloads.DirectionalPayload;
import com.createcivilization.capitol.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.payloads.toclient.ClientPacketHandler;
import com.createcivilization.capitol.old.payloads.toserver.ServerPacketHandler;
import com.createcivilization.capitol.old.team.War;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record BiRemoveWar(War war) implements DirectionalPayload.BiDirectional {

	public static final Type<BiRemoveWar> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "bi_rem_war"));

	public static final StreamCodec<FriendlyByteBuf, BiRemoveWar> STREAM_CODEC =
		StreamCodec.composite(
			PacketHandler.WAR_DATA_CODEC, BiRemoveWar::war,
			BiRemoveWar::new
		);

	public static void client(Object payload, IPayloadContext context) {
		ClientPacketHandler.removeWar(((BiRemoveWar) payload).war());
	}

	public static void server(Object payload, IPayloadContext context) {
		ServerPacketHandler.removeWar(((BiRemoveWar) payload).war(), context.player());
	}

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
