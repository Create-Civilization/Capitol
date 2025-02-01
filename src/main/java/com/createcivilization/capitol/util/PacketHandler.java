package com.createcivilization.capitol.util;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.packets.toclient.gui.S2COpenTeamStatistics;
import com.createcivilization.capitol.packets.toclient.syncing.*;
import com.createcivilization.capitol.packets.toserver.*;
import com.createcivilization.capitol.packets.toserver.syncing.C2SRequestSync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.*;

public class PacketHandler {

	private static final String PROTOCOL_VERSION = "1";

	private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(Capitol.MOD_ID, "info_channel"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals
	);

	public static <T> void handlePacketWithContext(
		T packet,
		Supplier<NetworkEvent.Context> contextSupplier,
		BiConsumer<T, NetworkEvent.Context> packetHandler
	) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(() -> packetHandler.accept(packet,ctx));
		ctx.setPacketHandled(true);
	}

	public static void handleSyncedPacket(Runnable toRun) {
		try {
			toRun.run();
		} catch (NullPointerException e) {
			System.out.println("Exception encountered on " + toRun.getClass().getCanonicalName() + " packet handling, dumping data and requesting synchronization.");
			TeamUtils.loadedTeams.clear();
			PacketHandler.sendToServer(new C2SRequestSync());
		}
	}

	public static <T> void generalAddPacket(
		Class<T> packet,
		int id,
		BiConsumer<T, FriendlyByteBuf> encoder,
		Function<FriendlyByteBuf, T> decoder,
		BiConsumer<T,NetworkEvent.Context> handler,
		NetworkDirection direction
	) {
		INSTANCE.messageBuilder(packet, id, direction)
			.encoder(encoder)
			.decoder(decoder)
			.consumerMainThread((packett, supplier) -> handlePacketWithContext(packett, supplier, handler))
			.add();
	}

	public static <T> void clientAddPacket(
		Class<T> packet,
		int id,
		BiConsumer<T, FriendlyByteBuf> encoder,
		Function<FriendlyByteBuf, T> decoder,
		BiConsumer<T,NetworkEvent.Context> handler
	) {
		generalAddPacket(packet, id, encoder, decoder, (packett, context) -> handleSyncedPacket(() -> handler.accept(packett, context)), NetworkDirection.PLAY_TO_SERVER);
	}

	public static <T> void serverAddPacket(
		Class<T> packet,
		int id,
		BiConsumer<T, FriendlyByteBuf> encoder,
		Function<FriendlyByteBuf, T> decoder,
		BiConsumer<T,NetworkEvent.Context> handler
	) {
		generalAddPacket(packet, id, encoder, decoder, handler, NetworkDirection.PLAY_TO_CLIENT);
	}

	public static void register() {
		int id = 0;

		// S2C packets
		serverAddPacket(S2COpenTeamStatistics.class, id++, S2COpenTeamStatistics::encode, S2COpenTeamStatistics::new, S2COpenTeamStatistics::handle);
		serverAddPacket(S2CAddChunk.class, id++, S2CAddChunk::encode, S2CAddChunk::new, S2CAddChunk::handle);
		serverAddPacket(S2CAddTeam.class, id++, S2CAddTeam::encode, S2CAddTeam::new, S2CAddTeam::handle);
		serverAddPacket(S2CRemoveChunk.class, id++, S2CRemoveChunk::encode, S2CRemoveChunk::new, S2CRemoveChunk::handle);
		serverAddPacket(S2CRemoveTeam.class, id++, S2CRemoveTeam::encode, S2CRemoveTeam::new, S2CRemoveTeam::handle);

		// C2S packets
		clientAddPacket(C2SRequestSync.class, id++, C2SRequestSync::encode, C2SRequestSync::new, C2SRequestSync::handle);
		clientAddPacket(C2SCreateTeam.class, id++, C2SCreateTeam::encode, C2SCreateTeam::new, C2SCreateTeam::handle);
		clientAddPacket(C2SClaimChunk.class, id++, C2SClaimChunk::encode, C2SClaimChunk::new, C2SClaimChunk::handle);
		clientAddPacket(C2SUnclaimChunk.class, id++, C2SUnclaimChunk::encode, C2SUnclaimChunk::new, C2SUnclaimChunk::handle);
		clientAddPacket(C2SClaimCurrentChunk.class, id++, C2SClaimCurrentChunk::encode, C2SClaimCurrentChunk::new, C2SClaimCurrentChunk::handle);
		clientAddPacket(C2SInvitePlayer.class, id++, C2SInvitePlayer::encode, C2SInvitePlayer::new, C2SInvitePlayer::handle);
		clientAddPacket(C2SSendTeamMessage.class, id, C2SSendTeamMessage::encode, C2SSendTeamMessage::new, C2SSendTeamMessage::handle);
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