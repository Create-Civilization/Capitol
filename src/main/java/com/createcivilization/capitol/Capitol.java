package com.createcivilization.capitol;

import net.minecraft.server.MinecraftServer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.slf4j.*;

import wiiu.mavity.util.ObjectHolder;

// git push origin main
// git pull
@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ObjectHolder<MinecraftServer> server = new ObjectHolder<>();

    @SuppressWarnings("removal") // Forge docs are gaslighting when it says to place FMLJavaModLoadingContext in the constructor
    public Capitol() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server.set(event.getServer()); // Cache server
    }
}