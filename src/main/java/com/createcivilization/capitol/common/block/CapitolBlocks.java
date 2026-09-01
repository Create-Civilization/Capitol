package com.createcivilization.capitol.common.block;

import com.createcivilization.capitol.Capitol;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CapitolBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
        DeferredRegister.createBlocks(Capitol.MOD_ID);

    public static final DeferredBlock<CapitolBlock> CAPITOL_BLOCK =
        BLOCKS.register("capitol_block", () -> new CapitolBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(5.0f, 6.0f)
                .requiresCorrectToolForDrops()
        ));
}