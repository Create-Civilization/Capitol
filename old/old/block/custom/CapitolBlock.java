package com.createcivilization.capitol.old.old.block.custom;

import com.createcivilization.capitol.old.old.block.entity.CapitolBlockEntity;
import com.createcivilization.capitol.old.old.config.CapitolConfig;
import com.createcivilization.capitol.old.old.payloads.bidirectional.PacketHandler;
import com.createcivilization.capitol.old.old.payloads.toclient.gui.S2COpenTeamStatistics;
import com.createcivilization.capitol.old.old.payloads.toclient.syncing.S2CRemoveCapitol;
import com.createcivilization.capitol.old.old.team.OldTeam;

import com.createcivilization.capitol.old.old.util.data.DistHelper;
import com.createcivilization.capitol.old.old.util.team.TeamUtils;
import com.mojang.serialization.MapCodec;

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
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return simpleCodec(CapitolBlock::new);
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

	@Override
	public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
		if (level.isClientSide) return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
		destroy(level, pos);
		return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
	}

	public static void destroy(Level level, BlockPos pos) {
		ResourceLocation dimension = level.dimension().location();
		ChunkPos chunkPos = new ChunkPos(pos);
		ObjectHolder<OldTeam> team = TeamUtils.getTeam(chunkPos, dimension);
		team.ifPresent(
			team1 -> {
				OldTeam.TeamDimensionData dimensionalData = team1.getDimensionalData(dimension);
				dimensionalData.getParentOfChunk(chunkPos)
					.ifPresent( capitolData ->
						{
							dimensionalData.removeCapitolData(capitolData);
							PacketHandler.sendToAllPlayers(new S2CRemoveCapitol(capitolData, dimension, team1));
						}
					);
			}
		);
	}

	// setPlacedBy --> Minecraft
	// Check if:
	// Player is in receivingOldTeam
	// Chunk does not have CapitolBlock
	// Then:
	// Claim chunk & chunk radius (CONFIG AMOUNT, DEFAULTING TO 1)
	// Else:
	// Break block
	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(world, pos, state, placer, stack);

		if (
			!world.isClientSide
				&& world.getBlockEntity(pos) instanceof CapitolBlockEntity // Safety check
				&& placer instanceof Player player // Make sure nothing else is placing it
				&& TeamUtils.hasTeam(player) // Make sure player has a receivingOldTeam
				&& !TeamUtils.chunkHasCapitolBlock(new ChunkPos(pos), TeamUtils.getPlayerDimension(player))
				&& !TeamUtils.isInClaimedChunk(player, pos)
		) {
			OldTeam oldTeam = TeamUtils.getTeam(player).getOrThrow();
			ResourceLocation dimension = world.dimension().location();
			ChunkPos chunk = new ChunkPos(pos);
			TeamUtils.claimChunk(oldTeam, dimension, chunk);
			TeamUtils.claimChunkRadius(
                    oldTeam,
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
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		ObjectHolder<OldTeam> team = TeamUtils.getTeam(new ChunkPos(pos), level.dimension().location());
		if (team.isEmpty()) return InteractionResult.FAIL;

		DistHelper.runWhenOnServer(() -> () -> PacketHandler.sendToPlayer(new S2COpenTeamStatistics(team.getOrThrow().getTeamId()), (ServerPlayer) player));

		return InteractionResult.CONSUME;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
}