package com.createcivilization.capitol.common.block;

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

public class CapitolBlock extends HorizontalDirectionalBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // How many chunks outward from the capitol block to claim
    // 3 means a 7x7 area (the chunk it's in, plus 3 outward in each direction)
    private static final int CLAIM_RADIUS = 3;

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

        if (team.getCapitolPos() != null) {
            player.sendSystemMessage(
                Component.literal("Your team already has a Capitol Block placed.")
                    .withStyle(ChatFormatting.RED)
            );
            level.removeBlock(pos, false);
            player.addItem(new ItemStack(CapitolBlocks.CAPITOL_BLOCK.get()));
            return;
        }

        // Claim the surrounding chunks
        ChunkPos centerChunk = new ChunkPos(pos);
        int claimed = 0;

        for (int dx = -CLAIM_RADIUS; dx <= CLAIM_RADIUS; dx++) {
            for (int dz = -CLAIM_RADIUS; dz <= CLAIM_RADIUS; dz++) {
                ChunkPos chunkPos = new ChunkPos(
                    centerChunk.x + dx,
                    centerChunk.z + dz
                );

                // Skip if already claimed by anyone
                if (DatabaseManager.database.getChunkOwner(chunkPos, level) != null) {
                    continue;
                }

                DatabaseManager.database.claimChunk(team, chunkPos, level);
                S2CChunkData packet = new S2CChunkData(chunkPos.toLong(), team);
                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, chunkPos, packet);
                claimed++;
            }
        }

        // Store the capitol block position on the team
        DatabaseManager.database.setCapitolPos(
            team,
            pos,
            level.dimension().location().toString()
        );

        player.sendSystemMessage(
            Component.literal("Capitol Block placed! Claimed " + claimed + " chunks around it.")
                .withStyle(ChatFormatting.GREEN)
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        Team team = DatabaseManager.database.getTeamByCapitolPos(
            pos,
            level.dimension().location().toString()
        );
        if (team == null) return InteractionResult.PASS;

        PacketDistributor.sendToPlayer(
            (ServerPlayer) player,
            new S2COpenCapitolScreen(team, pos)
        );
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        // Only run on the server, and only if the block is actually being removed
        // (not just changing state)
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            unclaimRadius(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void unclaimRadius(Level level, BlockPos pos) {
        ChunkPos centerChunk = new ChunkPos(pos);

        // Find what team owns the chunk the capitol block was in
        Team team = DatabaseManager.database.getTeamByCapitolPos(
            pos,
            level.dimension().location().toString()
        );
        if (team == null) return;

        for (int dx = -CLAIM_RADIUS; dx <= CLAIM_RADIUS; dx++) {
            for (int dz = -CLAIM_RADIUS; dz <= CLAIM_RADIUS; dz++) {
                ChunkPos chunkPos = new ChunkPos(
                    centerChunk.x + dx,
                    centerChunk.z + dz
                );
                DatabaseManager.database.unclaimChunk(team, chunkPos, level);
                S2CChunkRemove packet = new S2CChunkRemove(chunkPos.toLong());
                PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, chunkPos, packet);
            }
        }

        // Clear the stored capitol position from the team
        DatabaseManager.database.setCapitolPos(team, null, null);
    }
}