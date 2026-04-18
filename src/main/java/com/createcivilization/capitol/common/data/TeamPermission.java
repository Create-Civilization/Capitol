package com.createcivilization.capitol.common.data;

public enum TeamPermission {
	MOB_GREIFING             (1L << 0),
	FIRE_SPREADING           (1L << 1),
	EXPLOSIONS               (1L << 2);

	private final long bit;

	TeamPermission(long bit) {
		this.bit = bit;
	}

	public static long of(TeamPermission... permissions) {
		long bits = 0L;
		for (TeamPermission permission : permissions) {
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

