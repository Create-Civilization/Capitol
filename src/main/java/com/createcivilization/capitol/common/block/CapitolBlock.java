package com.createcivilization.capitol.common.block;

import com.createcivilization.capitol.common.data.CapitolBlockData;
import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import com.createcivilization.capitol.common.networking.packets.S2CChunkData;
import com.createcivilization.capitol.common.networking.packets.S2CChunkRemove;
import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolScreen;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CapitolBlock extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CapitolBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return simpleCodec(CapitolBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        // Only run on the server
        if (level.isClientSide()) return;
        if (!(placer instanceof ServerPlayer player)) return;

        Team team = DatabaseManager.database.getPlayerTeam(player);

        if (team == null) {
            player.sendSystemMessage(
                Component.literal("You must be in a team to place the Capitol Block.")
                    .withStyle(ChatFormatting.RED)
            );
            // Remove the block since they can't use it
            level.removeBlock(pos, false);
            // Give the item back
            player.addItem(new ItemStack(CapitolBlocks.CAPITOL_BLOCK.get()));
            return;
        }

        String dimension = level.dimension().location().toString();

        // first block a team places (or the first after all of them got destroyed)
        // becomes the Capital. one per team.
        boolean isCapital = !DatabaseManager.database.teamHasCapital(team);

        CapitolTier tier = null;
        if (!isCapital) {
            // extra blocks need permission to place...
            if (!Permission.MANAGE_CAPITOL_BLOCKS.hasPermission(
                DatabaseManager.database.getPlayerPermission(player, team))) {
                player.sendSystemMessage(
                    Component.literal("You do not have permission to place additional Capitol Blocks.")
                        .withStyle(ChatFormatting.RED)
                );
                level.removeBlock(pos, false);
                player.addItem(new ItemStack(CapitolBlocks.CAPITOL_BLOCK.get()));
                return;
            }
            // ...and have to go inside the team's own claims.
            Team chunkOwner = DatabaseManager.database.getChunkOwner(new ChunkPos(pos), level);
            if (chunkOwner == null || !chunkOwner.getId().equals(team.getId())) {
                player.sendSystemMessage(
                    Component.literal("Additional Capitol Blocks must be placed inside your team's claimed chunks.")
                        .withStyle(ChatFormatting.RED)
                );
                level.removeBlock(pos, false);
                player.addItem(new ItemStack(CapitolBlocks.CAPITOL_BLOCK.get()));
                return;
            }
            // extra blocks start as a Village, upgradeable later
            tier = CapitolTier.VILLAGE;
        }

        long capitolBlockId = DatabaseManager.database.addCapitolBlock(team, pos, dimension, isCapital, tier);

        if (isCapital) {
            // the Capital grabs everything around it on placement
            ChunkPos centerChunk = new ChunkPos(pos);
            int claimed = 0;

            for (int dx = -CapitolBlockData.CAPITAL_CLAIM_RADIUS; dx <= CapitolBlockData.CAPITAL_CLAIM_RADIUS; dx++) {
                for (int dz = -CapitolBlockData.CAPITAL_CLAIM_RADIUS; dz <= CapitolBlockData.CAPITAL_CLAIM_RADIUS; dz++) {
                    ChunkPos chunkPos = new ChunkPos(
                        centerChunk.x + dx,
                        centerChunk.z + dz
                    );

                    // Skip if already claimed by anyone
                    if (DatabaseManager.database.getChunkOwner(chunkPos, level) != null) {
                        continue;
                    }

                    DatabaseManager.database.claimChunk(team, chunkPos, level, capitolBlockId);
                    S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team);
                    PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, chunkPos, packet);
                    claimed++;
                }
            }

            player.sendSystemMessage(
                Component.literal("Capitol Block placed! This block is now your team's Capital. Claimed " + claimed + " chunks around it.")
                    .withStyle(ChatFormatting.GREEN)
            );
        } else {
            // extra blocks don't claim new stuff, they just take over
            // the team's already-claimed chunks in a 5x5 around them
            int transferred = DatabaseManager.database.transferClaimedChunksToCapitolBlock(
                team, level, pos, capitolBlockId, CapitolBlockData.ADDITIONAL_CLAIM_RADIUS
            );
            player.sendSystemMessage(
                Component.literal("Capitol Block placed! This block is now a Village, controlling " + transferred + " claimed chunk(s).")
                    .withStyle(ChatFormatting.GREEN)
            );
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        String dimension = level.dimension().location().toString();
        CapitolBlockData data = DatabaseManager.database.getCapitolBlock(pos, dimension);
        if (data == null) return InteractionResult.PASS;

        Team team = DatabaseManager.database.getTeam(data.teamId());
        if (team == null) return InteractionResult.PASS;

        boolean canUpgrade = !data.capital()
            && data.tier() != null
            && data.tier().upgraded() != null
            && Permission.MANAGE_CAPITOL_BLOCKS.hasPermission(
                DatabaseManager.database.getPlayerPermission(player, team));

        PacketDistributor.sendToPlayer(
            (ServerPlayer) player,
            new S2COpenCapitolScreen(team, pos, data.capital(), data.tier(), canUpgrade)
        );
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        // Only run on the server, and only if the block is actually being removed
        // (not just changing state)
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            unclaimCapitolBlock(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void unclaimCapitolBlock(Level level, BlockPos pos) {
        String dimension = level.dimension().location().toString();

        CapitolBlockData data = DatabaseManager.database.getCapitolBlock(pos, dimension);
        if (data == null) return;

        Team team = DatabaseManager.database.getTeam(data.teamId());
        if (team == null) return;

        // only this block's chunks get unclaimed; ones the team claimed
        // on their own (or belong to other blocks) stay
        List<ChunkPos> removed = DatabaseManager.database.unclaimCapitolBlockChunks(
            team, data.id(), dimension
        );
        for (ChunkPos chunkPos : removed) {
            S2CChunkRemove packet = new S2CChunkRemove(chunkPos.toLong());
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, chunkPos, packet);
        }

        // Remove the block's record (this also clears the Capital designation if the block was the Capital)
        DatabaseManager.database.removeCapitolBlock(team, pos, dimension);
    }
}
