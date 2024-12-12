package com.createcivilization.capitol.block.custom;

import com.createcivilization.capitol.block.entity.CapitolBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

public class CapitolBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(0,0,0, 16,16,16);

    public CapitolBlock(Properties properties) {
        super(properties);
    }

    @Override
	@SuppressWarnings("all")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
	@SuppressWarnings("all")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
	@SuppressWarnings("all")
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CapitolBlockEntity(blockPos, blockState);
    }
}
