package com.createcivilization.capitol.util;

import com.createcivilization.capitol.team.Team;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
	public static List<Boolean> permissionToList(Permission permission) throws InvocationTargetException, IllegalAccessException {
		List<Boolean> permissionList = new ArrayList<>();
		RecordComponent[] components = permission.getClass().getRecordComponents();

		for (RecordComponent component : components) {
			Object value = component.getAccessor().invoke(permission);
			permissionList.add((Boolean) value);
		}
		return permissionList;
	}
	public static Permission getPermission(Team team, Player player){
		return team.getPermission(team.getPlayerRole(player.getUUID()));
	}
	public static Permission listToPermission(List<Boolean> listPermission) {
		return new Permission(
			listPermission.get(0),
			listPermission.get(1),
			listPermission.get(2),
			listPermission.get(3),
			listPermission.get(4),
			listPermission.get(5)
		);
	}
}
