package com.createcivilization.capitol.server.networking;

import com.createcivilization.capitol.common.block.CapitolBlocks;
import com.createcivilization.capitol.common.config.CapitolConfig;
import com.createcivilization.capitol.common.data.CapitolBlockData;
import com.createcivilization.capitol.common.data.CapitolMember;
import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.data.TeamMember;
import com.createcivilization.capitol.common.item.SubClaimWand;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import com.createcivilization.capitol.common.networking.packets.C2SChunkRequest;
import com.createcivilization.capitol.common.networking.packets.C2SClaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SDamageWand;
import com.createcivilization.capitol.common.networking.packets.C2SInvitePlayer;
import com.createcivilization.capitol.common.networking.packets.C2SNameCapitolBlock;
import com.createcivilization.capitol.common.networking.packets.C2SCancelCapitolBlockNaming;
import com.createcivilization.capitol.common.networking.packets.C2SSetCapitolBlockMayor;
import com.createcivilization.capitol.common.networking.packets.C2STeamChat;
import com.createcivilization.capitol.common.networking.packets.C2SUnclaimChunk;
import com.createcivilization.capitol.common.networking.packets.C2SUpgradeCapitolBlock;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolNamingScreen;
import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolScreen;
import com.createcivilization.capitol.common.networking.packets.S2CSyncWars;
import com.createcivilization.capitol.common.networking.packets.C2SDeclareWar;
import com.createcivilization.capitol.common.networking.packets.C2SEndWar;
import com.createcivilization.capitol.common.data.War;
import com.createcivilization.capitol.common.events.WarEvent;
import com.createcivilization.capitol.server.events.WarTakeoverEvents;
import com.createcivilization.capitol.server.invites.InviteHandler;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ServerPayloadHandler {

	public static void handleChunkRequest(final C2SChunkRequest request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		ChunkPos chunkPos = new ChunkPos(request.packedChunkPos());
		Team team = database.getChunkOwner(chunkPos, context.player().level());
		if (team == null) {
			S2CChunkRemove packet = new S2CChunkRemove(request.packedChunkPos());
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) context.player().level(), chunkPos, packet);
			return;
		}
		S2CChunkData packet = new S2CChunkData(
			request.packedChunkPos(),
			team,
			Optional.ofNullable(database.getCapitolBlockIdForChunk(chunkPos, context.player().level()))
		);
		PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) context.player().level(), chunkPos, packet);
	}

	private static boolean isOutsideClaimRadius(Player player, ChunkPos targetChunk) {
		int claimRadius = CapitolConfig.CLAIM_RADIUS.get();
		if (claimRadius <= 0) return false;
		ChunkPos playerChunk = player.chunkPosition();
		return Math.abs(playerChunk.x - targetChunk.x) > claimRadius
			|| Math.abs(playerChunk.z - targetChunk.z) > claimRadius;
	}

	public static void handleClaimChunk(final C2SClaimChunk request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.literal("You are not in any team").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.CLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.literal("You do not have permission to claim chunks").withStyle(ChatFormatting.RED), false);
			return;
		}

		int serverLimit = CapitolConfig.MAX_TEAM_CLAIMS.get();
		int teamLimit = team.getMaxClaims();
		int effectiveLimit = (teamLimit > 0) ? Math.min(serverLimit, teamLimit) : serverLimit;
		int remaining = Math.max(0, effectiveLimit - team.getCurrentClaims());
		if (remaining <= 0) {
			boolean isServerLimit = (teamLimit <= 0) || (serverLimit <= teamLimit);
			String limitName = isServerLimit ? "server" : "team";
			int limitValue = isServerLimit ? serverLimit : teamLimit;
			player.displayClientMessage(Component.literal("Your team has reached the " + limitName + " claim limit (" + limitValue + ")").withStyle(ChatFormatting.RED), false);
			return;
		}

		int claimed = 0;
		boolean skippedAdjacent = false;
		long[] packed = request.packedChunkPositions();

		// Multi-chunk claims (like a JourneyMap rectangle) count as one connected group:
		// a chunk is claimable if it touches our territory, or another chunk in the
		// selection that does. Teams with no claims yet have to start inside a
		// capitol block's claim area.
		Set<Long> batchChunks = new HashSet<>();
		for (long packedPos : packed) {
			batchChunks.add(packedPos);
		}

		Set<Long> claimable = new HashSet<>();
		Deque<Long> frontier = new ArrayDeque<>();
		for (long packedPos : batchChunks) {
			ChunkPos chunkPos = new ChunkPos(packedPos);
			boolean adjacent;
			if (team.getCurrentClaims() <= 0) {
				adjacent = database.isChunkInCapitolBlockRadius(team, chunkPos, player.level());
			} else {
				adjacent = false;
				for (Direction dir : Direction.Plane.HORIZONTAL) {
					ChunkPos neighbor = new ChunkPos(chunkPos.x + dir.getStepX(), chunkPos.z + dir.getStepZ());
					Team owner = database.getChunkOwner(neighbor, player.level());
					if (owner != null && owner.getId().equals(team.getId())) {
						adjacent = true;
						break;
					}
				}
			}
			if (!adjacent) continue;
			claimable.add(packedPos);
			frontier.add(packedPos);
		}
		while (!frontier.isEmpty()) {
			long packedPos = frontier.poll();
			ChunkPos chunkPos = new ChunkPos(packedPos);
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				long neighborPacked = ChunkPos.asLong(chunkPos.x + dir.getStepX(), chunkPos.z + dir.getStepZ());
				if (batchChunks.contains(neighborPacked) && !claimable.contains(neighborPacked)) {
					claimable.add(neighborPacked);
					frontier.add(neighborPacked);
				}
			}
		}

		for (long packedPos : packed) {
			if (claimed >= remaining) break;

			ChunkPos chunkPos = new ChunkPos(packedPos);
			if (isOutsideClaimRadius(player, chunkPos)) continue;

			Team existingOwner = database.getChunkOwner(chunkPos, player.level());
			if (existingOwner != null) continue;

			if (!claimable.contains(packedPos)) {
				skippedAdjacent = true;
				continue;
			}

			database.claimChunk(team, chunkPos, player.level());
			S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team, Optional.empty());
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);
			claimed++;
		}

		player.displayClientMessage(Component.literal("Claimed " + claimed + " chunk(s)").withStyle(ChatFormatting.GREEN), false);
		if (claimed == 0 && skippedAdjacent) {
			if (database.getTeamCapitolBlocks(team, player.level().dimension().location().toString()).isEmpty()) {
				player.displayClientMessage(Component.literal("Your team needs a Capitol Block before you can claim chunks").withStyle(ChatFormatting.RED), false);
			} else {
				player.displayClientMessage(Component.translatable("commands.capitol.claim.not_adjacent").withStyle(ChatFormatting.RED), false);
			}
		}
	}

	public static void handleUnclaimChunk(final C2SUnclaimChunk request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.literal("You are not in any team").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.UNCLAIM_CHUNKS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.literal("You do not have permission to unclaim chunks").withStyle(ChatFormatting.RED), false);
			return;
		}

		int unclaimed = 0;
		long[] packed = request.packedChunkPositions();
		for (long packedPos : packed) {
			ChunkPos chunkPos = new ChunkPos(packedPos);
			if (isOutsideClaimRadius(player, chunkPos)) continue;

			Team existingOwner = database.getChunkOwner(chunkPos, player.level());
			if (existingOwner == null) continue;
			if (!existingOwner.getId().equals(team.getId())) continue;

			database.unclaimChunk(team, chunkPos, player.level());
			S2CChunkRemove packet = new S2CChunkRemove(chunkPos.toLong());
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);
			unclaimed++;
		}

		player.displayClientMessage(Component.literal("Unclaimed " + unclaimed + " chunk(s)").withStyle(ChatFormatting.GREEN), false);
	}

	public static void handleInvitePlayer(final C2SInvitePlayer request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.translatable("commands.capitol.not_in_team_error").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.INVITE_MEMBERS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.translatable("commands.capitol.team.invite.no_permission").withStyle(ChatFormatting.RED), false);
			return;
		}

		ServerPlayer playerToInvite = player.getServer() == null ? null : player.getServer().getPlayerList().getPlayerByName(request.playerName());
		if (playerToInvite == null) {
			player.displayClientMessage(Component.translatable("commands.capitol.team.invite.invalid_player").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (database.getPlayerTeam(playerToInvite) != null) {
			player.displayClientMessage(Component.translatable("commands.capitol.team.invite.target_in_team").withStyle(ChatFormatting.RED), false);
			return;
		}

		InviteHandler.addInvite(playerToInvite, team);
		player.displayClientMessage(Component.translatable("commands.capitol.team.invite.success",
			Component.literal(playerToInvite.getName().getString()).withStyle(ChatFormatting.WHITE))
			.withStyle(ChatFormatting.GRAY), false);
	}

	public static void handleDamageWand(C2SDamageWand packet, IPayloadContext context) {
		ServerPlayer player = (ServerPlayer) context.player();
		ItemStack held = player.getMainHandItem();
		if (held.getItem() instanceof SubClaimWand) {
			held.hurtAndBreak(1, (ServerLevel) player.level(), player, item -> {});
		}
	}

	// the naming screen confirmed: register the block (name is globally unique)
	// and claim/take over its chunks, same as placement used to do
	public static void handleNameCapitolBlock(final C2SNameCapitolBlock request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null || !team.getId().equals(request.teamId())) {
			// left the team between placing and naming: drop the block
			removeUnregisteredBlock(player, request.pos());
			player.displayClientMessage(Component.literal("You are no longer in that team, block removed").withStyle(ChatFormatting.RED), false);
			return;
		}

		String name = request.name().trim();
		if (name.isBlank() || name.length() > 32) {
			reopenNamingScreen(player, request.pos(), request.teamId());
			return;
		}

		String dimension = player.level().dimension().location().toString();
		if (database.getCapitolBlock(request.pos(), dimension) != null) {
			// already registered (double confirm or whatever), just show the book
			reopenBook(player, request.pos(), dimension);
			return;
		}

		if (database.capitolBlockNameExists(name)) {
			player.displayClientMessage(Component.literal("That name is already taken").withStyle(ChatFormatting.RED), false);
			reopenNamingScreen(player, request.pos(), request.teamId());
			return;
		}

		boolean isCapital = !database.teamHasCapital(team);
		CapitolTier tier = isCapital ? null : CapitolTier.VILLAGE;
		long capitolBlockId = database.addCapitolBlock(
			team, request.pos(), dimension, isCapital, tier, name, database.getTeamLeader(team)
		);

		if (isCapital) {
			// the Capital grabs everything around it on placement
			ChunkPos centerChunk = new ChunkPos(request.pos());
			int claimed = 0;
			for (int dx = -CapitolBlockData.CAPITAL_CLAIM_RADIUS; dx <= CapitolBlockData.CAPITAL_CLAIM_RADIUS; dx++) {
				for (int dz = -CapitolBlockData.CAPITAL_CLAIM_RADIUS; dz <= CapitolBlockData.CAPITAL_CLAIM_RADIUS; dz++) {
					ChunkPos chunkPos = new ChunkPos(centerChunk.x + dx, centerChunk.z + dz);

					// skip if already claimed by anyone
					if (database.getChunkOwner(chunkPos, player.level()) != null) continue;

					database.claimChunk(team, chunkPos, player.level(), capitolBlockId);
					S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team, Optional.of(capitolBlockId));
					PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);
					claimed++;
				}
			}
			player.displayClientMessage(
				Component.literal("Capitol Block '" + name + "' placed! This block is now your team's Capital. Claimed " + claimed + " chunks around it.")
					.withStyle(ChatFormatting.GREEN), false);
		} else {
			// extra blocks don't claim new stuff, they just take over
			// the team's already-claimed chunks in a 5x5 around them
			List<ChunkPos> transferred = database.transferClaimedChunksToCapitolBlock(
				team, player.level(), request.pos(), capitolBlockId, CapitolBlockData.ADDITIONAL_CLAIM_RADIUS
			);
			for (ChunkPos chunkPos : transferred) {
				S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team, Optional.of(capitolBlockId));
				PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) player.level(), chunkPos, packet);
			}
			player.displayClientMessage(
				Component.literal("Capitol Block '" + name + "' placed! This block is now a Village, controlling " + transferred.size() + " claimed chunk(s).")
					.withStyle(ChatFormatting.GREEN), false);
		}
	}

	// the player closed the naming screen without confirming: remove the block + refund it
	public static void handleCancelCapitolBlockNaming(final C2SCancelCapitolBlockNaming request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null || !team.getId().equals(request.teamId())) return;

		String dimension = player.level().dimension().location().toString();
		if (database.getCapitolBlock(request.pos(), dimension) != null) return;

		removeUnregisteredBlock(player, request.pos());
	}

	public static void handleSetCapitolBlockMayor(final C2SSetCapitolBlockMayor request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.literal("You are not in a team").withStyle(ChatFormatting.RED), false);
			return;
		}

		String dimension = player.level().dimension().location().toString();
		CapitolBlockData data = database.getCapitolBlock(request.pos(), dimension);
		if (data == null) {
			player.displayClientMessage(Component.literal("There is no Capitol Block at that position").withStyle(ChatFormatting.RED), false);
			return;
		}
		if (!data.teamId().equals(team.getId())) {
			player.displayClientMessage(Component.literal("That Capitol Block does not belong to your team").withStyle(ChatFormatting.RED), false);
			return;
		}
		if (data.capital()) {
			player.displayClientMessage(Component.literal("The Capital's mayor is always the team leader").withStyle(ChatFormatting.RED), false);
			return;
		}
		if (!Permission.MANAGE_CAPITOL_BLOCKS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.literal("You do not have permission to assign mayors").withStyle(ChatFormatting.RED), false);
			return;
		}

		boolean isMember = database.getTeamMembers(team).stream()
			.anyMatch(member -> member.playerUUID().equals(request.mayorUuid()));
		if (!isMember) {
			player.displayClientMessage(Component.literal("That player is not in your team").withStyle(ChatFormatting.RED), false);
			return;
		}

		database.setCapitolBlockMayor(data.id(), request.mayorUuid());
		player.displayClientMessage(Component.literal("Mayor assigned").withStyle(ChatFormatting.GREEN), false);

		// reopen the book so the new mayor shows
		reopenBook(player, request.pos(), dimension);
	}

	public static void handleUpgradeCapitolBlock(final C2SUpgradeCapitolBlock request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(Component.literal("You are not in a team").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.MANAGE_CAPITOL_BLOCKS.hasPermission(database.getPlayerPermission(player, team))) {
			player.displayClientMessage(Component.literal("You do not have permission to upgrade Capitol Blocks").withStyle(ChatFormatting.RED), false);
			return;
		}

		String dimension = player.level().dimension().location().toString();
		CapitolBlockData data = database.getCapitolBlock(request.pos(), dimension);
		if (data == null) {
			player.displayClientMessage(Component.literal("There is no Capitol Block at that position").withStyle(ChatFormatting.RED), false);
			return;
		}
		if (!data.teamId().equals(team.getId())) {
			player.displayClientMessage(Component.literal("That Capitol Block does not belong to your team").withStyle(ChatFormatting.RED), false);
			return;
		}
		if (data.capital()) {
			player.displayClientMessage(Component.literal("The Capital cannot be upgraded").withStyle(ChatFormatting.RED), false);
			return;
		}

		CapitolTier next = data.tier() == null ? null : data.tier().upgraded();
		if (next == null) {
			player.displayClientMessage(Component.literal("This Capitol Block is already at its maximum tier").withStyle(ChatFormatting.RED), false);
			return;
		}

		database.setCapitolBlockTier(data.id(), next);
		player.displayClientMessage(
			Component.literal("Capitol Block upgraded to " + next + "! Max claims: " + next.maxClaims() + ". Upkeep: " + next.upkeep() + ".")
				.withStyle(ChatFormatting.GREEN), false);

		// reopen the book so the new tier shows
		reopenBook(player, request.pos(), dimension);
	}

	public static void handleTeamChat(final C2STeamChat request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team team = database.getPlayerTeam(player);
		if (team == null) {
			player.displayClientMessage(
				Component.literal("You are not in a team").withStyle(ChatFormatting.RED),
				false
			);
			return;
		}

		// getRGB() returns ARGB; mask off the alpha channel so TextColor.fromRgb gets a plain 24-bit RGB value
		int rgb = team.getColor().getRGB() & 0xFFFFFF;

		Component message = Component.empty()
			.append(Component.literal("[" + team.getName() + "] ")
				.withStyle(s -> s.withColor(TextColor.fromRgb(rgb))))
			.append(Component.literal("<" + player.getName().getString() + "> ")
				.withStyle(ChatFormatting.WHITE))
			.append(Component.literal(request.message())
				.withStyle(ChatFormatting.WHITE));

		for (TeamMember member : database.getTeamMembers(team)) {
			ServerPlayer online = context.player().getServer()
				.getPlayerList()
				.getPlayer(member.playerUUID());
			if (online != null) {
				online.sendSystemMessage(message);
			}
		}
	}

	public static void handleDeclareWar(final C2SDeclareWar request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team declaring = database.getPlayerTeam(player);
		if (declaring == null) {
			player.displayClientMessage(Component.literal("You are not in any team").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.DECLARE_WAR.hasPermission(database.getPlayerPermission(player, declaring))) {
			player.displayClientMessage(Component.literal("You do not have permission to declare war").withStyle(ChatFormatting.RED), false);
			return;
		}

		Team receiving = database.getTeamByName(request.receivingTeamName());
		if (receiving == null) {
			player.displayClientMessage(Component.literal("There is no team called \"" + request.receivingTeamName() + "\"").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (receiving.getId().equals(declaring.getId())) {
			player.displayClientMessage(Component.literal("You cannot declare war on your own team").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (database.warExists(declaring.getId(), receiving.getId())) {
			player.displayClientMessage(Component.literal("Your team is already at war with \"" + receiving.getName() + "\"").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!database.addWar(declaring, receiving)) return;

		War war = database.getWar(declaring.getId(), receiving.getId());
		if (war != null) NeoForge.EVENT_BUS.post(new WarEvent.WarCreatedEvent(war));
		broadcastWars();

		player.displayClientMessage(
			Component.literal("Successfully declared war on \"" + receiving.getName() + "\"").withStyle(ChatFormatting.GREEN),
			true
		);
	}

	public static void handleEndWar(final C2SEndWar request, final IPayloadContext context) {
		CapitolDatabase database = DatabaseManager.database;
		Player player = context.player();

		Team declaring = database.getPlayerTeam(player);
		if (declaring == null) {
			player.displayClientMessage(Component.literal("You are not in any team").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!declaring.getId().equals(request.declaringTeamId())) {
			player.displayClientMessage(Component.literal("Only the declaring team can end this war").withStyle(ChatFormatting.RED), false);
			return;
		}

		if (!Permission.DECLARE_WAR.hasPermission(database.getPlayerPermission(player, declaring))) {
			player.displayClientMessage(Component.literal("You do not have permission to end wars").withStyle(ChatFormatting.RED), false);
			return;
		}

		War war = database.getWar(request.declaringTeamId(), request.receivingTeamId());
		if (war == null) {
			player.displayClientMessage(Component.literal("That war no longer exists").withStyle(ChatFormatting.RED), false);
			return;
		}

		database.removeWar(request.declaringTeamId(), request.receivingTeamId());
		WarTakeoverEvents.clearWarState(war);

		broadcastWars();

		player.displayClientMessage(
			Component.literal("War successfully ended").withStyle(ChatFormatting.GREEN),
			true
		);
	}

	// re-sends the full war list to every connected player
	public static void broadcastWars() {
		PacketDistributor.sendToAllPlayers(new S2CSyncWars(DatabaseManager.database.getAllWars()));
	}

	// builds the book-open packet with everything the book page needs
	public static S2COpenCapitolScreen openCapitolScreenFor(Team team, CapitolBlockData data, Player player) {
		CapitolDatabase database = DatabaseManager.database;
		long perms = database.getPlayerPermission(player, team);
		boolean canUpgrade = !data.capital()
			&& data.tier() != null
			&& data.tier().upgraded() != null
			&& Permission.MANAGE_CAPITOL_BLOCKS.hasPermission(perms);
		boolean canAssignMayor = !data.capital()
			&& Permission.MANAGE_CAPITOL_BLOCKS.hasPermission(perms);

		List<CapitolMember> members = new ArrayList<>();
		for (TeamMember member : database.getTeamMembers(team)) {
			members.add(new CapitolMember(member.playerUUID(), playerName(player, member.playerUUID())));
		}

		return new S2COpenCapitolScreen(
			team,
			data.pos(),
			data.capital(),
			data.tier(),
			canUpgrade,
			data.name(),
			data.mayorUuid() == null ? null : playerName(player, data.mayorUuid()),
			members,
			canAssignMayor,
			perms
		);
	}

	private static void reopenBook(Player player, BlockPos pos, String dimension) {
		CapitolDatabase database = DatabaseManager.database;
		Team team = database.getPlayerTeam(player);
		CapitolBlockData data = database.getCapitolBlock(pos, dimension);
		if (team == null || data == null) return;
		PacketDistributor.sendToPlayer((ServerPlayer) player, openCapitolScreenFor(team, data, player));
	}

	private static void reopenNamingScreen(Player player, BlockPos pos, UUID teamId) {
		CapitolDatabase database = DatabaseManager.database;
		Team team = database.getTeam(teamId);
		if (team == null) return;
		boolean isCapital = !database.teamHasCapital(team);
		PacketDistributor.sendToPlayer((ServerPlayer) player, new S2COpenCapitolNamingScreen(pos, teamId, isCapital, isCapital ? null : CapitolTier.VILLAGE));
	}

	private static void removeUnregisteredBlock(Player player, BlockPos pos) {
		var level = player.level();
		if (level.getBlockState(pos).is(CapitolBlocks.CAPITOL_BLOCK.get())) {
			level.removeBlock(pos, false);
			player.addItem(new ItemStack(CapitolBlocks.CAPITOL_BLOCK.get()));
		}
	}

	// display name for a player, works for offline ones too via the profile cache
	private static String playerName(Player requester, UUID uuid) {
		ServerPlayer online = requester.getServer().getPlayerList().getPlayer(uuid);
		if (online != null) return online.getName().getString();
		var profile = requester.getServer().getProfileCache().get(uuid);
		return profile.map(gameProfile -> gameProfile.getName()).orElse("Unknown");
	}
}