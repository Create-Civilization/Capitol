package com.createcivilization.capitol.util;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.toclient.gui.S2CopenTeamStatistics;
import com.createcivilization.capitol.packets.toclient.syncing.S2CaddChunk;
import com.createcivilization.capitol.packets.toclient.syncing.S2CaddTeam;
import com.createcivilization.capitol.packets.toclient.syncing.S2CremoveChunk;
import com.createcivilization.capitol.packets.toclient.syncing.S2CremoveTeam;
import com.createcivilization.capitol.packets.toserver.C2ScreateTeam;
import com.createcivilization.capitol.packets.toserver.syncing.C2SrequestSync;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

	private static final String PROTOCOL_VERSION = "1";

	private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(Capitol.MOD_ID, "info_channel"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals
	);

	public static void register() {
		int id = 0;

		// S2C packets
		INSTANCE.messageBuilder(S2CaddTeam.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CaddTeam::encode)
			.decoder(S2CaddTeam::new)
			.consumerMainThread(S2CaddTeam::handle)
			.add();

		INSTANCE.messageBuilder(S2CremoveTeam.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CremoveTeam::encode)
			.decoder(S2CremoveTeam::new)
			.consumerMainThread(S2CremoveTeam::handle)
			.add();

		INSTANCE.messageBuilder(S2CaddChunk.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CaddChunk::encode)
			.decoder(S2CaddChunk::new)
			.consumerMainThread(S2CaddChunk::handle)
			.add();

		INSTANCE.messageBuilder(S2CremoveChunk.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CremoveChunk::encode)
			.decoder(S2CremoveChunk::new)
			.consumerMainThread(S2CremoveChunk::handle)
			.add();

		INSTANCE.messageBuilder(S2CopenTeamStatistics.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CopenTeamStatistics::encode)
			.decoder(S2CopenTeamStatistics::new)
			.consumerMainThread(S2CopenTeamStatistics::handle)
			.add();

		// C2S packets
		INSTANCE.messageBuilder(C2SrequestSync.class, id++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(C2SrequestSync::encode)
			.decoder(C2SrequestSync::new)
			.consumerMainThread(C2SrequestSync::handle)
			.add();

		INSTANCE.messageBuilder(C2ScreateTeam.class, id++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(C2ScreateTeam::encode)
			.decoder(C2ScreateTeam::new)
			.consumerMainThread(C2ScreateTeam::handle)
			.add();
	}

	public static void sendToServer(Object msg) {
		INSTANCE.sendToServer(msg);
	}

	public static void sendToPlayer(Object msg, ServerPlayer player) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), msg);
	}

	public static void sendToAllClients(Object msg) {
		INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
	}
}
