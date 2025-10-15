package com.createcivilization.capitol;

import com.createcivilization.capitol.old.block.CapitolBlocks;
import com.createcivilization.capitol.old.block.entity.CapitolBlockEntities;
import com.createcivilization.capitol.old.config.CapitolConfig;
import com.createcivilization.capitol.old.item.CapitolItems;

import com.createcivilization.capitol.server.ServerSetup;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

import org.slf4j.Logger;

@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";
	public static final Logger LOGGER = LogUtils.getLogger();

    public Capitol(IEventBus modEventBus, ModContainer container) {

		// Add listeners
		modEventBus.addListener(ServerSetup::init);

		// Register configs
		container.registerConfig(ModConfig.Type.SERVER, CapitolConfig.SERVER_SPEC);

		// Register custom objects
        CapitolItems.register(modEventBus);
        CapitolBlocks.register(modEventBus);
        CapitolBlockEntities.register(modEventBus);
    }
}