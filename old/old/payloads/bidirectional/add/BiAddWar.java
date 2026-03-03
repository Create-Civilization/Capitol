package com.createcivilization.capitol.old.old.payloads.bidirectional.add;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.old.payloads.DirectionalPayload;
import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.old.payloads.toclient.ClientPacketHandler;
import com.createcivilization.capitol.old.old.payloads.toserver.ServerPacketHandler;
import com.createcivilization.capitol.old.old.team.OldTeam;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record BiAddWar(OldTeam declaringOldTeam, OldTeam receivingOldTeam) implements DirectionalPayload.BiDirectional {

	public static final CustomPacketPayload.Type<BiAddWar> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "bi_add_war"));

	public static final StreamCodec<FriendlyByteBuf, BiAddWar> STREAM_CODEC =
		StreamCodec.composite(
			PacketHandler.TEAM_CODEC, BiAddWar::declaringOldTeam,
			PacketHandler.TEAM_CODEC, BiAddWar::receivingOldTeam,
			BiAddWar::new
		);

	public static void client(Object payload, IPayloadContext context) {
		BiAddWar addWar = (BiAddWar) payload;
		ClientPacketHandler.addWar(addWar.declaringOldTeam(), addWar.receivingOldTeam());
	}

	public static void server(Object payload, IPayloadContext context) {
		BiAddWar addWar = (BiAddWar) payload;

		Player player = context.player();
		if (!(TeamUtils.getTeam(player).get() instanceof OldTeam oldTeam)) return;
		if (!TeamUtils.canPlayerDo(oldTeam, player, "declareWar")) return;

		ServerPacketHandler.addWar(addWar.declaringOldTeam(), addWar.receivingOldTeam());
	}

	@Override
	public StreamCodec codec() {
		return STREAM_CODEC;
	}

	@Override
	public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
