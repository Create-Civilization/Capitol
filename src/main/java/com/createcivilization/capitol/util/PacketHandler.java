package com.createcivilization.capitol.util;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.toclient.gui.S2COpenTeamStatistics;
import com.createcivilization.capitol.packets.toclient.syncing.*;
import com.createcivilization.capitol.packets.toserver.C2SCreateTeam;
import com.createcivilization.capitol.packets.toserver.C2SClaimChunk;
import com.createcivilization.capitol.packets.toserver.C2SInvitePlayer;
import com.createcivilization.capitol.packets.toserver.syncing.C2SRequestSync;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.*;
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
		INSTANCE.messageBuilder(S2CAddTeam.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CAddTeam::encode)
			.decoder(S2CAddTeam::new)
			.consumerMainThread(S2CAddTeam::handle)
			.add();

		INSTANCE.messageBuilder(S2CRemoveTeam.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CRemoveTeam::encode)
			.decoder(S2CRemoveTeam::new)
			.consumerMainThread(S2CRemoveTeam::handle)
			.add();

		INSTANCE.messageBuilder(S2CAddChunk.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CAddChunk::encode)
			.decoder(S2CAddChunk::new)
			.consumerMainThread(S2CAddChunk::handle)
			.add();

		INSTANCE.messageBuilder(S2CRemoveChunk.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CRemoveChunk::encode)
			.decoder(S2CRemoveChunk::new)
			.consumerMainThread(S2CRemoveChunk::handle)
			.add();

		INSTANCE.messageBuilder(S2COpenTeamStatistics.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2COpenTeamStatistics::encode)
			.decoder(S2COpenTeamStatistics::new)
			.consumerMainThread(S2COpenTeamStatistics::handle)
			.add();

		INSTANCE.messageBuilder(S2CAddPlayerName.class, id++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(S2CAddPlayerName::encode)
			.decoder(S2CAddPlayerName::new)
			.consumerMainThread(S2CAddPlayerName::handle)
			.add();

		// C2S packets
		INSTANCE.messageBuilder(C2SRequestSync.class, id++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(C2SRequestSync::encode)
			.decoder(C2SRequestSync::new)
			.consumerMainThread(C2SRequestSync::handle)
			.add();

		INSTANCE.messageBuilder(C2SCreateTeam.class, id++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(C2SCreateTeam::encode)
			.decoder(C2SCreateTeam::new)
			.consumerMainThread(C2SCreateTeam::handle)
			.add();

		INSTANCE.messageBuilder(C2SClaimChunk.class, id++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(C2SClaimChunk::encode)
			.decoder(C2SClaimChunk::new)
			.consumerMainThread(C2SClaimChunk::handle)
			.add();

		INSTANCE.messageBuilder(C2SInvitePlayer.class, id++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(C2SInvitePlayer::encode)
			.decoder(C2SInvitePlayer::new)
			.consumerMainThread(C2SInvitePlayer::handle)
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