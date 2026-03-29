# Database
Capitol uses SQLite (`capitol.db`) stored in the world save folder. `DatabaseManager` handles the connection lifecycle — init on server start, close on server stop. Pragmas: WAL mode, synchronous NORMAL, foreign keys ON.

### Schema
- **teams** — `id` (UUID text, PK), `name`, `color` (ARGB int), `created_at` (epoch ms)
- **team_members** — `team_id` + `player_uuid` (composite PK), `role` (text), `permissions` (bitfield int). FK to teams with CASCADE delete
- **chunks** — `dimension` + `chunk_x` + `chunk_z` (composite PK), `team_id`. FK to teams with CASCADE delete

### Access Layer
`CapitolDatabase` (extends abstract `Database`) handles all CRUD operations via `PreparedStatement` with parameter binding:
- Team ops: `addTeam`, `removeTeam`, `getTeam`, `getPlayerTeam`
- Member ops: `addPlayerToTeam`, `removePlayerFromTeam`, `updatePlayerRole`, `getPlayerRole`
- Chunk ops: `claimChunk`, `unclaimChunk`, `getTeamChunks`, `unclaimAllChunks`
- Query ops: `hasChunkAt`, `getChunkOwner`, `getPermissionInChunk`, `isPlayerInTeam`
