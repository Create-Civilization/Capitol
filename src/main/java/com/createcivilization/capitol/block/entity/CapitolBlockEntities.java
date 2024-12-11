package com.createcivilization.capitol.block.entity;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.block.CapitolBlocks;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public class CapitolBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Capitol.MOD_ID);

    public static final RegistryObject<BlockEntityType<CapitolBlockEntity>> CAPITOL_BE =
            BLOCK_ENTITIES.register("capitol_block_entity", () ->
                    BlockEntityType.Builder.of(CapitolBlockEntity::new,
                            CapitolBlocks.CAPITOL_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}