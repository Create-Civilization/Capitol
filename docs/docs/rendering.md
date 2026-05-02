# Rendering
Both renderers hook into `RenderLevelStageEvent` at `AFTER_BLOCK_ENTITIES` and read from `ClientClaimCache` to know which chunks are claimed and by whom.

### BorderRenderer
Draws colored lines along chunk edges where territory borders another team or unclaimed land. The line hugs the terrain — it only draws on exposed block faces (solid block with air adjacent).

- Only renders chunks that are currently loaded on the client (`hasChunk` filter), so only visible land is processed
- Scans the full world height (`minBuildHeight` to `maxBuildHeight`), so borders remain visible when flying above terrain
- Three quad types: **horizontal** (top/bottom of blocks), **outward** (vertical face toward neighbor), **lateral** (bridges height gaps between columns)
- Horizontal and lateral quads fade from `BORDER_ALPHA` at the edge to transparent inward (`LINE_THICKNESS = 0.25` blocks wide)
- `Z_FIGHT_OFFSET` nudges geometry slightly off block surfaces to prevent flickering

### BorderWallRenderer
Draws a full-height transparent wall at chunk borders that fades in as the player approaches.

- `REVEAL_RADIUS = 8.0` — wall is invisible beyond this distance
- `INNER_RADIUS = 1.5` — wall fades out as the player walks into it (plane-distance based)
- `MAX_ALPHA = 0.35` — peak opacity, keeps walls subtle
- Per-vertex alpha based on 3D distance to player, multiplied by plane fade for smooth pass-through