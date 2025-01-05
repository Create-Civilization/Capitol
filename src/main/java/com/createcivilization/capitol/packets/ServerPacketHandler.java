package com.createcivilization.capitol.packets;

import com.createcivilization.capitol.util.TeamUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerPacketHandler {
	public static void syncDataWithPlayer(ServerPlayer sender) {
		TeamUtils.synchronizeServerDataWithPlayer(sender);
	}
}
