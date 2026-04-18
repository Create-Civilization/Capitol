package com.createcivilization.capitol.common.data;

import com.createcivilization.capitol.common.config.CapitolConfig;

/**
 * Bitfield-based protection categories that teams can toggle for their claims.
 * Stored in the {@code team_permissions} column on the {@code teams} table.
 * A set bit means protection is ENABLED (the event is blocked).
 */
public enum TeamProtection {

	EXPLOSION       (1L << 0, "explosion"),
	MOB_GRIEFING    (1L << 1, "mob_griefing"),
	FIRE            (1L << 2, "fire"),
	PISTON          (1L << 3, "piston"),
	FLUID_FLOW      (1L << 4, "fluid_flow"),
	DISPENSER       (1L << 5, "dispenser"),
	CROP_TRAMPLING  (1L << 6, "crop_trampling"),
	SCULK           (1L << 7, "sculk");

	private final long bit;
	private final String key;

	TeamProtection(long bit, String key) {
		this.bit = bit;
		this.key = key;
	}

	public String getKey() { return key; }

	public boolean hasProtection(long bits) {
		return (bits & this.bit) != 0;
	}

	public long set(long bits) {
		return bits | this.bit;
	}

	public long clear(long bits) {
		return bits & ~this.bit;
	}

	public long toggle(long bits) {
		return bits ^ this.bit;
	}

	/** Computes the default team_permissions value from server config. */
	public static long configDefaults() {
		long bits = 0L;
		for (TeamProtection tp : values()) {
			if (tp.getConfigDefault()) bits = tp.set(bits);
		}
		return bits;
	}

	public boolean getConfigDefault() {
		return switch (this) {
			case EXPLOSION -> CapitolConfig.DEFAULT_EXPLOSION.get();
			case MOB_GRIEFING -> CapitolConfig.DEFAULT_MOB_GRIEFING.get();
			case FIRE -> CapitolConfig.DEFAULT_FIRE.get();
			case PISTON -> CapitolConfig.DEFAULT_PISTON.get();
			case FLUID_FLOW -> CapitolConfig.DEFAULT_FLUID_FLOW.get();
			case DISPENSER -> CapitolConfig.DEFAULT_DISPENSER.get();
			case CROP_TRAMPLING -> CapitolConfig.DEFAULT_CROP_TRAMPLING.get();
			case SCULK -> CapitolConfig.DEFAULT_SCULK.get();
		};
	}
}
