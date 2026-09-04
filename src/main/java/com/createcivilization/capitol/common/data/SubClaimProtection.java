package com.createcivilization.capitol.common.data;

/**
 * Bitfield-based protection categories that subclaim owners can toggle for their subclaims.
 * Stored in the {@code protections} column on the {@code sub_claims} table.
 * A set bit means protection is ENABLED (the event is blocked).
 */
public enum SubClaimProtection {

	EXPLOSION       (1L << 0, "explosion"),
	MOB_GRIEFING    (1L << 1, "mob_griefing"),
	FIRE            (1L << 2, "fire"),
	PISTON          (1L << 3, "piston"),
	FLUID_FLOW      (1L << 4, "fluid_flow"),
	CROP_TRAMPLING  (1L << 6, "crop_trampling");

	private final long bit;
	private final String key;

	SubClaimProtection(long bit, String key) {
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

	/** Computes the default protections value: every protection enabled by default. */
	public static long configDefaults() {
		long bits = 0L;
		for (SubClaimProtection sp : values()) {
			if (sp.getConfigDefault()) bits = sp.set(bits);
		}
		return bits;
	}

	public boolean getConfigDefault() {
		return true;
	}
}