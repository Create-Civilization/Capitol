package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import org.jetbrains.annotations.Nullable;

public class C2SClaimChunk {

	private @Nullable BlockPos pos;

	public C2SClaimChunk() {}

	public C2SClaimChunk(@Nullable BlockPos pos) {
		this.pos = pos;
	}

	public C2SClaimChunk(FriendlyByteBuf friendlyByteBuf) {
		this.pos = friendlyByteBuf.readBlockPos();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		if (pos != null) friendlyByteBuf.writeBlockPos(this.pos);
	}

	public void handle(NetworkEvent.Context context) {
		var sender = context.getSender();
		assert sender != null;
		if (pos == null) pos = sender.getOnPos();
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.claimChunk(sender, pos), context);
	}
}