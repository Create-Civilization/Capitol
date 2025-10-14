package com.createcivilization.capitol.old.block.entity;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.old.block.CapitolBlocks;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CapitolBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Capitol.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CapitolBlockEntity>> CAPITOL_BE =
		BLOCK_ENTITIES.register(
			"capitol_block_entity",
			() -> new BlockEntityType<>(CapitolBlockEntity::new, ImmutableSet.of(CapitolBlocks.CAPITOL_BLOCK.get()), null)
		);

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}