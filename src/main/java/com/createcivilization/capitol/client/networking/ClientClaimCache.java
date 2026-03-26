package com.createcivilization.capitol.client.networking;

import com.createcivilization.capitol.common.data.Team;
import com.createcivilization.capitol.server.commands.Claim;
import org.joml.Vector3f;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

public class ClientClaimCache {

	public static Map<Vector3f, Team> claims = new HashMap<Vector3f, Team>();

}
