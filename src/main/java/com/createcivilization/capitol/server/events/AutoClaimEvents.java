package com.createcivilization.capitol.server.events;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

@EventBusSubscriber(modid = Capitol.MOD_ID, value = Dist.DEDICATED_SERVER)
public class AutoClaimEvents {
	private static final Map<UUID, Long> autoClaimingPlayers = new HashMap<>();

	@SubscribeEvent
	public static void onEntityEnterSection(EntityEvent.EnteringSection event) {
		if (!event.didChunkChange() || !(event.getEntity() instanceof Player player)) {
			return;
		}
		if (isAutoClaimer(player)) {
			if (Calendar.getInstance().getTimeInMillis() - autoClaimingPlayers.get(player.getUUID()) < CapitolConfig.AUTO_CLAIM_COOLDOWN.get() * 1000) {
				player.displayClientMessage(Component.translatable("commands.capitol.claim.auto_claiming.too_fast").withStyle(ChatFormatting.RED), false);
				return;
			}

			CapitolDatabase database = DatabaseManager.database;

			Team team = database.getPlayerTeam(player);
			if (team == null) {
				player.displayClientMessage(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED), false);
				return;
			}

			if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
				player.displayClientMessage(Component.translatable("commands.capitol.claim.no_permission").withStyle(ChatFormatting.RED), false);
				return;
			}

			int serverLimit = CapitolConfig.MAX_TEAM_CLAIMS.get();
			int teamLimit = team.getMaxClaims();
			int effectiveLimit = (teamLimit > 0) ? Math.min(serverLimit, teamLimit) : serverLimit;

			if (team.getCurrentClaims() >= effectiveLimit) {
				boolean isServerLimit = (teamLimit <= 0) || (serverLimit <= teamLimit);
				String limitName = isServerLimit ? "server" : "team";
				int limitValue = isServerLimit ? serverLimit : teamLimit;
				player.displayClientMessage(Component.translatable("commands.capitol.claim.limit_reached", limitName, limitValue).withStyle(ChatFormatting.RED), false);
				return;
			}

			ChunkPos chunkPos = player.chunkPosition();
			Team existingOwner = database.getChunkOwner(chunkPos, player.level());
			if (existingOwner != null) {
				player.displayClientMessage(Component.translatable("commands.capitol.claim.already_claimed", existingOwner.getName()).withStyle(ChatFormatting.RED), false);
				return;
			}

			if (!database.isChunkAdjacentToOwnClaim(team, chunkPos, player.level())) {
				player.displayClientMessage(Component.translatable("commands.capitol.claim.not_adjacent").withStyle(ChatFormatting.RED), false);
				return;
			}

			database.claimChunk(team, chunkPos, player.level());
			S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team);
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);

			player.displayClientMessage(Component.translatable("commands.capitol.claim.success").withStyle(ChatFormatting.GREEN), false);

			updateAutoClaimer(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent e) {
		removeAutoClaimer(e.getEntity()); // avoids a small memory leak, also disables auto-claiming on disconnect
	}

	/**
	 *	Updates the last time that a player has claimed a tile through auto claiming.
	 *	Adds the player to the list if they haven't already claimed.
	 *
	 * @param player player to be added / updated
	 */
	public static void updateAutoClaimer(Player player) {
		autoClaimingPlayers.put(player.getUUID(), System.currentTimeMillis());
	}

	public static void removeAutoClaimer(Player p) {
		autoClaimingPlayers.remove(p.getUUID());
	}

	public static boolean isAutoClaimer(Player p) {
		return autoClaimingPlayers.containsKey(p.getUUID());
	}
}
