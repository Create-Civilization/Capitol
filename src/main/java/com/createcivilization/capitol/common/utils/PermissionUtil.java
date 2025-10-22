package com.createcivilization.capitol.common.utils;

import com.createcivilization.capitol.old.config.CapitolConfig;

import java.util.*;

public class PermissionUtil {

	public static final ArrayList<String> permissions = new ArrayList<>(List.of(
		"claimChunks",
		"invitePlayers",
		"breakBlocks",
		"placeBlocks",
		"useItems",
		"interactEntities",
		"interactBlocks",
		"editRoles",
		"editPermissions",
		"removeMember",
		"declareWar"
	));

	public static Map<String, Boolean> newPermission(String keyword) {
		Map<String, Boolean> permission = new HashMap<>();
		return switch (keyword) {
			case "all_true" -> {
				for (String perm : permissions) permission.put(perm, true);
				yield permission;
			}
			case "all_false" -> {
				for (String perm : permissions) permission.put(perm, false);
				yield permission;
			}
			case "moderator" -> PermissionUtil.newPermission(
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				false
			);
			case "member" -> PermissionUtil.newPermission(
				true,
				true,
				true,
				true,
				true,
				true,
				true,
				false,
				false,
				false
			);
			case "non-member" -> PermissionUtil.newPermission(
				false,
				false,
				false,
				false,
				CapitolConfig.SERVER.nonMemberUseItems.get(),
				CapitolConfig.SERVER.nonMemberInteractEntities.get(),
				CapitolConfig.SERVER.nonMemberInteractBlocks.get(),
				false,
				false,
				false
			);
			default -> null;
		};
	}

	public static Map<String, Boolean> newPermission(Boolean... permissionsToPut) {
		return newPermission(List.of(permissionsToPut));
	}

	public static Map<String, Boolean> newPermission(Iterable<Boolean> permissionsToPut) {
		Map<String, Boolean> permission = new HashMap<>();
		int i = 0;
		for (Boolean permissionToPut : permissionsToPut) {
			permission.put(permissions.get(i), permissionToPut);
			i++;
		}
		return permission;
	}
}