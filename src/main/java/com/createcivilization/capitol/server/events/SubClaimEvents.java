package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.SubClaim;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CSubClaimData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class SubClaimEvents {

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;

		CapitolDatabase database = DatabaseManager.database;
		List<SubClaim> subClaims = database.getAllSubClaims();
		for (SubClaim subClaim : subClaims) {
			PacketDistributor.sendToPlayer(player, new S2CSubClaimData(
				subClaim.id(), subClaim.dimension(),
				subClaim.minX(), subClaim.minY(), subClaim.minZ(),
				subClaim.maxX(), subClaim.maxY(), subClaim.maxZ()
			));
		}
	}
}