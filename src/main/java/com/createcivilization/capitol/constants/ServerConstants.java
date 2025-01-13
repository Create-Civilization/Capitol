package com.createcivilization.capitol.constants;

import net.minecraft.server.MinecraftServer;

import net.minecraftforge.api.distmarker.*;

import wiiu.mavity.util.ObjectHolder;

@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerConstants {

	public static ObjectHolder<MinecraftServer> server = new ObjectHolder<>();

}