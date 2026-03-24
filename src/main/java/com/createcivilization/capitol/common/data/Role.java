package com.createcivilization.capitol.common.data;

public enum Role {
	OWNER("owner"),
	OFFICER("officer"),
	MEMBER("member");

	private final String id;

	Role(String id) {
		this.id = id;
	}

	public String getID() {
		return this.id;
	}

	public static Role fromID(String id) {
		for (Role role : Role.values()) {
			if (role.getID().equals(id)) {
				return role;
			}
		}
		throw new IllegalArgumentException("No such role: " + id);
	}
}
