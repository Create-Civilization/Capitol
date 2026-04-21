package com.createcivilization.capitol.common.compat.sable;

import com.createcivilization.capitol.common.managers.DatabaseManager;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;

public class SableObserver implements SubLevelObserver {

	@Override
	public void onSubLevelRemoved(SubLevel subLevel, SubLevelRemovalReason reason) {
		if (reason != SubLevelRemovalReason.REMOVED) return;
		DatabaseManager.database.removeSubLevel(subLevel.getUniqueId());
	}
}
