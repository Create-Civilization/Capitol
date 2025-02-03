package com.createcivilization.capitol;

import com.createcivilization.capitol.block.CapitolBlocks;
import com.createcivilization.capitol.block.entity.CapitolBlockEntities;
import com.createcivilization.capitol.config.CapitolConfig;
import com.createcivilization.capitol.item.CapitolItems;
import com.createcivilization.capitol.util.PacketHandler;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.Logger;

@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";
	public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal") // Putting FMLJavaModLoadingContext in the constructor is incompatible with versions below 47.3.10
    public Capitol() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Don't use FMLJavaModLoadingContext to register because it doesn't have this method below version 47.3.10
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CapitolConfig.SERVER_SPEC);

        modEventBus.addListener(this::commonSetup);

        CapitolItems.register(modEventBus);
        CapitolBlocks.register(modEventBus);
        CapitolBlockEntities.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

		@SuppressWarnings("PointlessBooleanExpression") // nuh uh
		boolean cake = true & false;
		System.out.println(cake);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(PacketHandler::register);
	}
}