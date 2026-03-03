package com.createcivilization.capitol.old.common.assets;

import com.createcivilization.capitol.old.common.utils.PermissionUtil;

import java.util.Map;

public record Role(
	String name,
	Map<String, Boolean> permissionMap
) {
	public static Role ownerRole() {
		return new Role(
			"owner",
			PermissionUtil.newPermission("all_true")
		);
	}
	public static Role moderatorRole() {
		return new Role(
			"moderator",
			PermissionUtil.newPermission("moderator")
		);
	}
	public static Role memberRole() {
		return new Role(
			"member",
			PermissionUtil.newPermission("member")
		);
	}
	public static Role nonMemberRole() {
		return new Role(
			"guest",
			PermissionUtil.newPermission("non-member")
		);
	}
	public static Role emptyRole(String name) {
		return new Role(
			name,
			PermissionUtil.newPermission("all_false")
		);
	}
}
