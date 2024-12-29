package com.createcivilization.capitol.block.custom;

import com.createcivilization.capitol.block.entity.CapitolBlockEntity;

import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class CapitolBlock extends BlockWithEntity {

    public static final VoxelShape SHAPE = Block.createCuboidShape(0,0,0, 16,16,16);

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public CapitolBlock(Settings settings) {
        super(settings);
		this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CapitolBlockEntity(blockPos, blockState);
    }

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	// onDestroyedByPlayer --> forge
	@Override
	public boolean onDestroyedByPlayer(BlockState state, World level, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
//		ResourceLocation dimension = level.dimension().location();
//		ChunkPos chunkPos = new ChunkPos(pos);
//		TeamUtils.unclaimChunkRadius(
//			Objects.requireNonNull(TeamUtils.getTeam(
//				chunkPos,
//				dimension
//			)).get(),
//			dimension,
//			chunkPos,
//			1
//		);
		// DISABLED FOR DEBUG PROTECTION

		return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
	}

	// setPlacedBy --> Minecraft
	// Check if:
	// Player is in team
	// Chunk does not have CapitolBlock
	// Then:
	// Claim chunk & chunk radius (CONFIG AMOUNT, DEFAULTING TO 1)
	// Else:
	// Break block
	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);

		if (
			!world.isClient()
				&& world.getBlockEntity(pos) instanceof CapitolBlockEntity // Safety check
				&& placer instanceof PlayerEntity player // Make sure nothing else is placing it
				&& TeamUtils.hasTeam(player) // Make sure player has a team
				&& !TeamUtils.hasCapitolBlock(new ChunkPos(pos), placer.getWorld().getRegistryKey().getValue())
				&& !TeamUtils.isInClaimedChunk(player, pos)
		) {
			Team team = TeamUtils.getTeam(player).getOrThrow();
			Identifier dimension = world.getRegistryKey().getValue();
			ChunkPos chunk = new ChunkPos(pos);
			team.addCapitolBlock(dimension, chunk);
			TeamUtils.claimChunkRadius(
				team,
				dimension,
				chunk,
				Config.claimRadius.getOrThrow() // It is NOT null.
			);
		} else {
			// Conditions not met, destroy
			world.breakBlock(pos, true);
		}
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}