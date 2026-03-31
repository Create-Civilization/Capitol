package com.createcivilization.capitol.common.data;

public enum Permission {
	// --- Administrative ---
	CLAIM_CHUNKS                (1L << 0),
	UNCLAIM_CHUNKS              (1L << 1),
	FORCELOAD_CHUNKS            (1L << 2),
	INVITE_MEMBERS              (1L << 3),
	KICK_MEMBERS                (1L << 4),
	MANAGE_ROLES                (1L << 5),  // create/edit/delete roles
	ASSIGN_ROLES                (1L << 6),  // change a member's role
	MANAGE_TEAM                 (1L << 7),  // rename team, disband, etc.

	// --- Block Protection ---
	BREAK_BLOCKS                (1L << 8),
	PLACE_BLOCKS                (1L << 9),
	INTERACT_BLOCKS             (1L << 10), // doors, buttons, levers, etc.
	OPEN_CONTAINERS             (1L << 11), // chests, barrels, hoppers, furnaces
	USE_REDSTONE                (1L << 12), // repeaters, comparators, etc.
	CROP_TRAMPLE                (1L << 13),
	FROST_WALKING               (1L << 14),

	// --- Entity Protection ---
	INTERACT_ENTITIES           (1L << 15), // villagers, item frames, armor stands
	KILL_ENTITIES               (1L << 16), // killing friendly entities
	KILL_HOSTILE                (1L << 17), // killing hostile entities
	PLAYER_ATTACK               (1L << 18), // PvP within claims
	ENTITY_GRIEFING             (1L << 19), // entities breaking blocks / damaging mobs

	// --- Item Protection ---
	USE_ITEMS                   (1L << 20), // buckets, flint & steel, etc.
	PICKUP_ITEMS                (1L << 21),
	PICKUP_XP                   (1L << 22),
	TOSS_ITEMS                  (1L << 23),
	MOB_LOOT                    (1L << 24), // loot drops from mob kills
	PLAYER_DEATH_LOOT           (1L << 25), // accessing player death drops

	// --- Redstone / Mechanical ---
	TRIGGER_PRESSURE_PLATES     (1L << 26),
	TRIGGER_TRIPWIRES           (1L << 27),
	TRIGGER_BUTTONS_PROJECTILES (1L << 28), // buttons hit by arrows
	TRIGGER_TARGETS_PROJECTILES (1L << 29), // target blocks hit by arrows
	DISPENSER_ACCESS            (1L << 30), // dispensers firing into claims
	PISTON_ACCESS               (1L << 31), // pistons pushing into claims
	FLUID_FLOW                  (1L << 32), // fluids flowing into claims

	// --- Portal / Teleportation ---
	USE_NETHER_PORTALS          (1L << 33),
	CHORUS_FRUIT_TELEPORT       (1L << 34),

	// --- Spawning ---
	NATURAL_SPAWN_HOSTILE       (1L << 35),
	NATURAL_SPAWN_FRIENDLY      (1L << 36),
	SPAWNER_HOSTILE             (1L << 37),
	SPAWNER_FRIENDLY            (1L << 38),

	// --- Explosions / Environmental ---
	BLOCK_EXPLOSIONS            (1L << 39), // explosion block damage
	ENTITY_EXPLOSIONS           (1L << 40), // explosion entity damage
	FIRE_SPREAD                 (1L << 41),
	LIGHTNING                   (1L << 42),
	RAIDS                       (1L << 43); // village raids


	private final long bit;

	Permission(long bit) {
		this.bit = bit;
	}

	public static long of(Permission... permissions) {
		long bits = 0L;
		for (Permission permission : permissions) {
			bits = permission.add(bits);
		}
		return bits;
	}

	public long add(long bits){
		return bits | this.bit;
	}

	public long remove(long bits){
		return bits & ~this.bit;
	}

	public long toggle(long bits){
		return bits ^ this.bit;
	}

	public boolean hasPermission(long bits){
		return (bits & this.bit) != 0;
	}
}

