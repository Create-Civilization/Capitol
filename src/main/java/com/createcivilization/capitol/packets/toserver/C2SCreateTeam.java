package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.awt.*;
import java.util.function.Supplier;

public class C2SCreateTeam {

	String teamName;
	Color teamColor;

	public C2SCreateTeam(String name, Color chosenColor) {
		this.teamName = name;
		this.teamColor = chosenColor;
	}

	public C2SCreateTeam(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.teamName = friendlyByteBuf.readUtf();
		int r = friendlyByteBuf.readInt();
		int g = friendlyByteBuf.readInt();
		int b = friendlyByteBuf.readInt();
		this.teamColor = new Color(r,g,b);
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.teamName);
		friendlyByteBuf.writeInt(teamColor.getRed());
		friendlyByteBuf.writeInt(teamColor.getGreen());
		friendlyByteBuf.writeInt(teamColor.getBlue());
	}

	public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ctx.enqueueWork(
			() -> DistExecutor.unsafeRunWhenOn(
				Dist.DEDICATED_SERVER,
				() -> () -> ServerPacketHandler.createTeam(this.teamName, ctx.getSender(), this.teamColor)
			)
		);
	}
}