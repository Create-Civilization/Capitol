package com.createcivilization.capitol.common.data;

import com.createcivilization.capitol.common.config.CapitolConfig;
import org.jetbrains.annotations.Nullable;

// tiers for extra (non-Capital) blocks. start at VILLAGE, upgrade to TOWN then CITY.
// the Capital itself isn't a tier - that's the is_capital flag, it has no tier.
public enum CapitolTier {
	VILLAGE,
	TOWN,
	CITY;

	// next tier up, or null at max
	@Nullable
	public CapitolTier upgraded() {
		return switch (this) {
			case VILLAGE -> TOWN;
			case TOWN -> CITY;
			case CITY -> null;
		};
	}

	// max claims for this tier (from config, not enforced yet)
	public int maxClaims() {
		return switch (this) {
			case VILLAGE -> CapitolConfig.CAPITOL_VILLAGE_MAX_CLAIMS.get();
			case TOWN -> CapitolConfig.CAPITOL_TOWN_MAX_CLAIMS.get();
			case CITY -> CapitolConfig.CAPITOL_CITY_MAX_CLAIMS.get();
		};
	}

	// upkeep per period for this tier (from config, not charged yet)
	public int upkeep() {
		return switch (this) {
			case VILLAGE -> CapitolConfig.CAPITOL_VILLAGE_UPKEEP.get();
			case TOWN -> CapitolConfig.CAPITOL_TOWN_UPKEEP.get();
			case CITY -> CapitolConfig.CAPITOL_CITY_UPKEEP.get();
		};
	}

	// reads a stored tier name; null for empty/unknown (the Capital has no tier)
	@Nullable
	public static CapitolTier byName(@Nullable String name) {
		if (name == null || name.isEmpty()) return null;
		try {
			return valueOf(name);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}