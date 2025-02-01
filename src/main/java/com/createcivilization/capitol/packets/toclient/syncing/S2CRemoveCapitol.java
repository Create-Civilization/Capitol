package com.createcivilization.capitol.packets.toclient.syncing;

import com.createcivilization.capitol.packets.ClientPacketHandler;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.GsonUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

public class S2CRemoveCapitol {
	private final Team.CapitolData capitolData;
	private final ResourceLocation dimension;
	private final String teamId;

	public S2CRemoveCapitol(Team.CapitolData capitolData, ResourceLocation dimension, Team team) {
		this.capitolData = capitolData;
		this.dimension = dimension;
		this.teamId = team.getTeamId();
	}

	public S2CRemoveCapitol(FriendlyByteBuf friendlyByteBuf) {
		// Decode
		this.capitolData = GsonUtil.deserializeCapitol(friendlyByteBuf.readUtf());
		this.dimension = friendlyByteBuf.readResourceLocation();
		this.teamId = friendlyByteBuf.readUtf();
	}

	public void encode(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(GsonUtil.serializeCapitol(this.capitolData));
		friendlyByteBuf.writeResourceLocation(dimension);
		friendlyByteBuf.writeUtf(this.teamId);
	}

	public void handle(NetworkEvent.Context context) {
		ClientPacketHandler.handlePacket(() -> ClientPacketHandler.removeCapitol(this.capitolData, this.dimension, this.teamId), context);
	}
}
