package com.createcivilization.capitol.packets.toserver;

import com.createcivilization.capitol.packets.ServerPacketHandler;

import net.minecraft.network.FriendlyByteBuf;

import net.minecraftforge.network.NetworkEvent;

import java.awt.*;

public class C2SCreateTeam {

	private final String teamName;
	private final Color teamColor;

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

	public void handle(NetworkEvent.Context context) {
		ServerPacketHandler.handlePacket(() -> ServerPacketHandler.createTeam(this.teamName, context.getSender(), this.teamColor), context);
	}
}