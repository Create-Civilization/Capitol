package com.createcivilization.capitol.common.compat.sable;

import dev.ryanhcode.sable.api.entity.EntitySubLevelUtil;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.entity.player.Player;

public class Sable {

	public static SubLevelAccess getPlayerSublevel(Player player){
		return EntitySubLevelUtil.getLastTrackingSubLevel(player);
	}

}
