package com.createcivilization.capitol.util;

import com.createcivilization.capitol.config.CapitolConfig;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.*;

public class PermissionUtil {

	public static final ArrayList<String> permissions = new ArrayList<>(List.of(
		"breakBlocks",
		"placeBlocks",
		"useItems",
		"interactEntities",
		"interactBlocks",
		"addRole",
		"editPermissions",
		"removeMember"
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
				true
			);
			case "member" -> PermissionUtil.newPermission(
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
				CapitolConfig.SERVER.nonMemberUseItems.get(),
				CapitolConfig.SERVER.nonMemberInteractEntities.get(),
				CapitolConfig.SERVER.nonMemberInteractBlocks.get(),
				false,
				false,
				false
			);
			default -> null; // Throw null pointer exception, luv ya :>
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
		};
		return permission;
	}

	public static void savePermission(JsonWriter writer, Map<String, Map<String, Boolean>> rolePermissions) throws IOException {
		writer.name("rolePermissions").beginObject();
		for (Map.Entry<String, Map<String, Boolean>> mapEntry : rolePermissions.entrySet()) {
			writer.name(mapEntry.getKey()).beginObject();
			for (Map.Entry<String, Boolean> subEntry : mapEntry.getValue().entrySet()) {
				writer.name(subEntry.getKey()).value(subEntry.getValue());
			}
			writer.endObject();
		}
		writer.endObject();
	}
}