package com.createcivilization.capitol;

import com.createcivilization.capitol.block.CapitolBlocks;
import com.createcivilization.capitol.block.entity.CapitolBlockEntities;
import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.item.CapitolItems;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import org.slf4j.Logger;

@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";
	public static final Logger LOGGER = LogUtils.getLogger();

    public Capitol() {
		var container = ModLoadingContext.get().getActiveContainer();
        IEventBus modEventBus = container.getEventBus();

		container.registerConfig(ModConfig.Type.SERVER, CapitolConfig.SERVER_SPEC);

        CapitolItems.register(modEventBus);
        CapitolBlocks.register(modEventBus);
        CapitolBlockEntities.register(modEventBus);
    }
}