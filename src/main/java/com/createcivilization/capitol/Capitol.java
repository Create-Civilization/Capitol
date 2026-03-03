package com.createcivilization.capitol;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Mod(Capitol.MOD_ID)
public class Capitol {

    public static final String MOD_ID = "capitol";
	public static final Logger LOGGER = LogUtils.getLogger();

    public Capitol(IEventBus modEventBus, ModContainer container) {

    }
}