package com.createcivilization.capitol.common.block;

import com.createcivilization.capitol.common.data.ClaimedChunk;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.managers.DatabaseManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CapitolBlock extends Block {

    // How many chunks outward from the capitol block to claim
    // 2 means a 5x5 area (the chunk it's in, plus 2 outward in each direction)
    private static final int CLAIM_RADIUS = 2;

    public CapitolBlock(Properties properties) {
        super(properties);
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
                claimed++;
            }
        }

        // Store the capitol block position on the team
        DatabaseManager.database.setCapitolPos(team, pos);

        player.sendSystemMessage(
            Component.literal("Capitol Block placed! Claimed " + claimed + " chunks around it.")
                .withStyle(ChatFormatting.GREEN)
        );
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
        Team team = DatabaseManager.database.getTeamByCapitolPos(pos);
        if (team == null) return;

        for (int dx = -CLAIM_RADIUS; dx <= CLAIM_RADIUS; dx++) {
            for (int dz = -CLAIM_RADIUS; dz <= CLAIM_RADIUS; dz++) {
                ChunkPos chunkPos = new ChunkPos(
                    centerChunk.x + dx,
                    centerChunk.z + dz
                );
                DatabaseManager.database.unclaimChunk(team, chunkPos, level);
            }
        }

        // Clear the stored capitol position from the team
        DatabaseManager.database.setCapitolPos(team, null);
    }
}