package com.createcivilization.capitol.block;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.block.custom.CapitolBlock;
import com.createcivilization.capitol.item.CapitolItems;

import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
public class CapitolBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Capitol.MOD_ID);

    public static final RegistryObject<Block> CAPITOL_BLOCK = registerBlock("capitol_block",
            () -> new CapitolBlock(BlockBehaviour.Properties.copy(Blocks.LECTERN).noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return CapitolItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}