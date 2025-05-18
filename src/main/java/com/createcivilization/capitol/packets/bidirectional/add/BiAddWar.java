package com.createcivilization.capitol.packets.bidirectional.add;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.DirectionalPayload;
import com.createcivilization.capitol.packets.bidirectional.PacketHandler;
import com.createcivilization.capitol.packets.toclient.ClientPacketHandler;
import com.createcivilization.capitol.packets.toserver.ServerPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record BiAddWar(Team declaringTeam, Team receivingTeam) implements DirectionalPayload.BiDirectional {

	public static final CustomPacketPayload.Type<BiAddWar> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "bi_add_war"));

	public static final StreamCodec<FriendlyByteBuf, BiAddWar> STREAM_CODEC =
		StreamCodec.composite(
			PacketHandler.TEAM_CODEC, BiAddWar::declaringTeam,
			PacketHandler.TEAM_CODEC, BiAddWar::receivingTeam,
			BiAddWar::new
		);

	public static void client(Object payload, IPayloadContext context) {
		BiAddWar addWar = (BiAddWar) payload;
		ClientPacketHandler.addWar(addWar.declaringTeam(), addWar.receivingTeam());
	}

	public static void server(Object payload, IPayloadContext context) {
		BiAddWar addWar = (BiAddWar) payload;

		Player player = context.player();
		if (!(TeamUtils.getTeam(player).get() instanceof Team team)) return;
		if (!TeamUtils.canPlayerDo(team, player, "declareWar")) return;

		ServerPacketHandler.addWar(addWar.declaringTeam(), addWar.receivingTeam());
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
