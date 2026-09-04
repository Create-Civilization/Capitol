package com.createcivilization.capitol.client.networking;

import java.util.UUID;

public record SubClaimRenderData(
	UUID id,
	String dimension,
	int minX, int minY, int minZ,
	int maxX, int maxY, int maxZ
) {
}