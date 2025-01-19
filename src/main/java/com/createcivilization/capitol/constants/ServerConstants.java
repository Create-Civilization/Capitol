package com.createcivilization.capitol.constants;

import net.minecraft.server.MinecraftServer;

import net.minecraftforge.api.distmarker.*;

import wiiu.mavity.wiiu_lib.util.ObjectHolder;

@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerConstants {

	public static ObjectHolder<MinecraftServer> server = new ObjectHolder<>();

}