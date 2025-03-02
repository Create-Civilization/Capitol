package com.createcivilization.capitol.block;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.block.custom.CapitolBlock;
import com.createcivilization.capitol.item.CapitolItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
public class CapitolBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Capitol.MOD_ID);

    public static final DeferredHolder<Block, CapitolBlock> CAPITOL_BLOCK = registerBlock("capitol_block",
            () -> new CapitolBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LECTERN).noOcclusion()));

    private static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> block) {
		DeferredHolder<Block, T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredHolder<Item, BlockItem> registerBlockItem(String name, DeferredHolder<Block, T> block) {
        return CapitolItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}