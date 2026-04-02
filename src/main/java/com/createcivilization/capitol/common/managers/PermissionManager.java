package com.createcivilization.capitol.common.managers;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.common.data.Permission;
import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.common.modules.database.CapitolDatabase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class PermissionManager {

	private PermissionManager() {}



	//TODO: Needs fixing. I am to lazy rn to add all the methods needed

	public static boolean playerHasPermission(Player player, Permission permission, Level level, ChunkPos pos){
		CapitolDatabase database = DatabaseManager.database;
		Team team = database.getChunkOwner(pos, level);
		if(team == null) return true;
		long perms = database.getPlayerPermission(player, team);
		return permission.hasPermission(perms);

	}

	public static boolean playerHasBypass(Player player) {
		return player.hasPermissions(4); // Is operator
	}
}
