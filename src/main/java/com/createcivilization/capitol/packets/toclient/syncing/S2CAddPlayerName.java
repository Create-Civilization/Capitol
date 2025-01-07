package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class S2CAddPlayerName {

	String playerName;
	UUID playerUUID;

	public S2CAddPlayerName(ServerPlayer player) {
		this.playerName = player.getName().getString();
		this.playerUUID = player.getUUID();
	}

	public S2CAddPlayerName(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.playerName = friendlyByteBuf.readUtf();
		this.playerUUID = friendlyByteBuf.readUUID();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.playerName);
		friendlyByteBuf.writeUUID(this.playerUUID);
	}

	public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.addPlayerInfo(this.playerName, this.playerUUID))
		);

		ctx.setPacketHandled(true);
	}
}
