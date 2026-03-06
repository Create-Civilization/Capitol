package com.createcivilization.capitol.common.managers;

import com.createcivilization.capitol.common.modules.database.Database;
import com.createcivilization.capitol.common.modules.database.DummyDatabase;

public class DatabaseManager {

	public static Database database = new DummyDatabase();
}