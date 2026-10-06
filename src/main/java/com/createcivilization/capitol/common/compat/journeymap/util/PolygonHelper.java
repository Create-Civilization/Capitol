package com.createcivilization.capitol.common.compat.journeymap.util;

import com.createcivilization.capitol.client.journeymap.ClientJMClaims;
import com.createcivilization.capitol.client.networking.ClientClaimCache;
import com.createcivilization.capitol.common.data.Team;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.model.MapPolygon;
import journeymap.api.v2.client.model.ShapeProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PolygonHelper {

	public static List<PolygonOverlay> buildClaimOverlays(String modId, ResourceKey<Level> dimension) {
		Map<UUID, TeamClaims> byTeam = new HashMap<>();
		Map<ChunkPos, Team> source = ClientJMClaims.instance().snapshot();
		Map<ChunkPos, Long> blockIds = ClientJMClaims.instance().blockIdSnapshot();
		if (source.isEmpty()) {
			source = new HashMap<>(ClientClaimCache.claims);
			blockIds = new HashMap<>(ClientClaimCache.capitolBlockIds);
		}
		for (Map.Entry<ChunkPos, Team> entry : new ArrayList<>(source.entrySet())) {
			Team team = entry.getValue();
			byTeam.computeIfAbsent(team.getId(), k -> new TeamClaims(team)).chunks.add(entry.getKey());
		}

		List<PolygonOverlay> overlays = new ArrayList<>();
		for (TeamClaims tc : byTeam.values()) {
			Color color = tc.team.getColor();
			ShapeProperties props = new ShapeProperties()
				.setFillColor(color.getRGB())
				.setStrokeColor(color.getRGB())
				.setFillOpacity(0.25f)
				.setStrokeOpacity(0.9f)
				.setStrokeWidth(2f);

			for (Set<ChunkPos> component : connectedComponents(tc.chunks)) {
				List<List<BlockPos>> loops = traceLoops(component);
				if (loops.isEmpty()) continue;

				int outerIdx = 0;
				long outerArea = -1;
				for (int i = 0; i < loops.size(); i++) {
					long a = bboxArea(loops.get(i));
					if (a > outerArea) { outerArea = a; outerIdx = i; }
				}

				MapPolygon outer = new MapPolygon(loops.get(outerIdx));
				List<MapPolygon> holes = new ArrayList<>();
				for (int i = 0; i < loops.size(); i++) {
					if (i != outerIdx) holes.add(new MapPolygon(loops.get(i)));
				}
				overlays.add(new PolygonOverlay(modId, dimension, props, outer, holes.isEmpty() ? null : holes));
			}
		}

		overlays.addAll(buildBlockDividers(modId, dimension, source, blockIds));
		return overlays;
	}

	// faint lines between chunks that fall under different capitol blocks, so you
	// can tell which block presides over which chunk within a team's claims
	private static List<PolygonOverlay> buildBlockDividers(String modId, ResourceKey<Level> dimension,
														  Map<ChunkPos, Team> source, Map<ChunkPos, Long> blockIds) {
		List<PolygonOverlay> overlays = new ArrayList<>();
		ShapeProperties props = new ShapeProperties()
			.setFillColor(0xFFFFFF)
			.setFillOpacity(0.3f)
			.setStrokeColor(0xFFFFFF)
			.setStrokeOpacity(0f)
			.setStrokeWidth(0f);

		for (Map.Entry<ChunkPos, Team> entry : source.entrySet()) {
			ChunkPos c = entry.getKey();
			Team team = entry.getValue();

			// only check north + west so each shared edge is drawn once
			ChunkPos north = new ChunkPos(c.x, c.z - 1);
			if (differentBlock(source, blockIds, team, c, north)) {
				overlays.add(edgeOverlay(modId, dimension, props,
					c.getMinBlockX(), c.getMinBlockZ(), c.getMaxBlockX() + 1, c.getMinBlockZ() + 1));
			}
			ChunkPos west = new ChunkPos(c.x - 1, c.z);
			if (differentBlock(source, blockIds, team, c, west)) {
				overlays.add(edgeOverlay(modId, dimension, props,
					c.getMinBlockX(), c.getMinBlockZ(), c.getMinBlockX() + 1, c.getMaxBlockZ() + 1));
			}
		}
		return overlays;
	}

	// same team, but the chunk on the other side belongs to a different capitol block
	private static boolean differentBlock(Map<ChunkPos, Team> source, Map<ChunkPos, Long> blockIds,
										  Team team, ChunkPos chunk, ChunkPos neighbor) {
		Team neighborTeam = source.get(neighbor);
		if (neighborTeam == null || !neighborTeam.getId().equals(team.getId())) return false;
		return !Objects.equals(blockIds.get(chunk), blockIds.get(neighbor));
	}

	private static PolygonOverlay edgeOverlay(String modId, ResourceKey<Level> dimension, ShapeProperties props,
											  int x1, int z1, int x2, int z2) {
		MapPolygon poly = new MapPolygon(List.of(
			new BlockPos(x1, 0, z1),
			new BlockPos(x2, 0, z1),
			new BlockPos(x2, 0, z2),
			new BlockPos(x1, 0, z2)
		));
		return new PolygonOverlay(modId, dimension, props, poly);
	}

	private static List<Set<ChunkPos>> connectedComponents(Set<ChunkPos> chunks) {
		List<Set<ChunkPos>> components = new ArrayList<>();
		Set<ChunkPos> unvisited = new HashSet<>(chunks);
		while (!unvisited.isEmpty()) {
			Set<ChunkPos> component = new HashSet<>();
			Deque<ChunkPos> stack = new ArrayDeque<>();
			ChunkPos start = unvisited.iterator().next();
			unvisited.remove(start);
			stack.push(start);
			while (!stack.isEmpty()) {
				ChunkPos c = stack.pop();
				component.add(c);
				ChunkPos[] neighbors = {
					new ChunkPos(c.x + 1, c.z),
					new ChunkPos(c.x - 1, c.z),
					new ChunkPos(c.x, c.z + 1),
					new ChunkPos(c.x, c.z - 1)
				};
				for (ChunkPos n : neighbors) {
					if (unvisited.remove(n)) stack.push(n);
				}
			}
			components.add(component);
		}
		return components;
	}

	private static List<List<BlockPos>> traceLoops(Set<ChunkPos> component) {
		Map<BlockPos, BlockPos> edges = new HashMap<>();
		for (ChunkPos c : component) {
			int minX = c.getMinBlockX();
			int minZ = c.getMinBlockZ();
			int maxX = c.getMaxBlockX() + 1;
			int maxZ = c.getMaxBlockZ() + 1;
			BlockPos nw = new BlockPos(minX, 0, minZ);
			BlockPos ne = new BlockPos(maxX, 0, minZ);
			BlockPos sw = new BlockPos(minX, 0, maxZ);
			BlockPos se = new BlockPos(maxX, 0, maxZ);
			if (!component.contains(new ChunkPos(c.x, c.z - 1))) edges.put(ne, nw);
			if (!component.contains(new ChunkPos(c.x - 1, c.z))) edges.put(nw, sw);
			if (!component.contains(new ChunkPos(c.x, c.z + 1))) edges.put(sw, se);
			if (!component.contains(new ChunkPos(c.x + 1, c.z))) edges.put(se, ne);
		}

		List<List<BlockPos>> loops = new ArrayList<>();
		while (!edges.isEmpty()) {
			BlockPos start = edges.keySet().iterator().next();
			List<BlockPos> loop = new ArrayList<>();
			BlockPos cur = start;
			while (cur != null) {
				loop.add(cur);
				BlockPos next = edges.remove(cur);
				if (next == null || next.equals(start)) break;
				cur = next;
			}
			List<BlockPos> simplified = collapseCollinear(loop);
			if (simplified.size() >= 3) loops.add(simplified);
		}
		return loops;
	}

	private static List<BlockPos> collapseCollinear(List<BlockPos> loop) {
		int n = loop.size();
		if (n < 3) return loop;
		List<BlockPos> out = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			BlockPos prev = loop.get((i - 1 + n) % n);
			BlockPos cur = loop.get(i);
			BlockPos next = loop.get((i + 1) % n);
			long dx1 = cur.getX() - prev.getX();
			long dz1 = cur.getZ() - prev.getZ();
			long dx2 = next.getX() - cur.getX();
			long dz2 = next.getZ() - cur.getZ();
			if (dx1 * dz2 - dx2 * dz1 != 0) out.add(cur);
		}
		return out.size() >= 3 ? out : loop;
	}

	private static long bboxArea(List<BlockPos> loop) {
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPos p : loop) {
			if (p.getX() < minX) minX = p.getX();
			if (p.getX() > maxX) maxX = p.getX();
			if (p.getZ() < minZ) minZ = p.getZ();
			if (p.getZ() > maxZ) maxZ = p.getZ();
		}
		return (long)(maxX - minX) * (maxZ - minZ);
	}

	public static MapPolygon createRangePolygon(ChunkPos center, int radius) {
		int minX = (center.x - radius) << 4;
		int minZ = (center.z - radius) << 4;
		int maxX = ((center.x + radius) << 4) + 16;
		int maxZ = ((center.z + radius) << 4) + 16;

		return new MapPolygon(List.of(
			new BlockPos(minX, 0, minZ),
			new BlockPos(maxX, 0, minZ),
			new BlockPos(maxX, 0, maxZ),
			new BlockPos(minX, 0, maxZ)
		));
	}

	private static class TeamClaims {
		final Team team;
		final Set<ChunkPos> chunks = new HashSet<>();
		TeamClaims(Team team) { this.team = team; }
	}
}
