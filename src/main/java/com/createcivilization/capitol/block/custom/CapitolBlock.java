package com.createcivilization.capitol.block.custom;

import com.createcivilization.capitol.block.entity.CapitolBlockEntity;
import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.packets.toclient.gui.S2COpenTeamStatistics;
import com.createcivilization.capitol.team.Team;
import com.createcivilization.capitol.util.*;

import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import org.jetbrains.annotations.Nullable;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class CapitolBlock extends BaseEntityBlock {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public CapitolBlock(Properties properties) {
        super(properties);
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CapitolBlockEntity(blockPos, blockState);
    }

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.getStateDefinition().any().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	// onDestroyedByPlayer --> forge
	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		ResourceLocation dimension = level.dimension().location();
		ChunkPos chunkPos = new ChunkPos(pos);
		ObjectHolder<Team> team = TeamUtils.getTeam(chunkPos, dimension);
		if (!team.isEmpty())
			TeamUtils.unclaimChunkAndUpdate(
				team.getOrThrow(),
				dimension,
				chunkPos
			);


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
	@SuppressWarnings("resource")
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(world, pos, state, placer, stack);

		if (
			!world.isClientSide
				&& world.getBlockEntity(pos) instanceof CapitolBlockEntity // Safety check
				&& placer instanceof Player player // Make sure nothing else is placing it
				&& TeamUtils.hasTeam(player) // Make sure player has a team
				&& !TeamUtils.hasCapitolBlock(new ChunkPos(pos), placer.level().dimension().location())
				&& !TeamUtils.isInClaimedChunk(player, pos)
		) {
			Team team = TeamUtils.getTeam(player).getOrThrow();
			ResourceLocation dimension = world.dimension().location();
			ChunkPos chunk = new ChunkPos(pos);
			team.addCapitolBlock(dimension, chunk);
			TeamUtils.claimChunkRadius(
				team,
				dimension,
				chunk,
				CapitolConfig.SERVER.claimRadius.get()
			);
		} else {
			// Conditions not met, destroy
			world.destroyBlock(pos, true);
		}
	}

	@Override
	public InteractionResult use(BlockState pState, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
		ObjectHolder<Team> team = TeamUtils.getTeam(new ChunkPos(pos), level.dimension().location());
		if (team.isEmpty()) return InteractionResult.FAIL;

		DistExecutor.unsafeRunWhenOn(
			Dist.DEDICATED_SERVER,
			() -> () -> PacketHandler.sendToPlayer(new S2COpenTeamStatistics(team.getOrThrow().getTeamId()), (ServerPlayer) player)
		);

		return InteractionResult.CONSUME;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}