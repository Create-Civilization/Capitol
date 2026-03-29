# Commands
Current commands are registered in `CapitolCommands` and are **temporary** — meant for testing during development, to be replaced with proper implementations.

### /claim (temporary)
- `/claim chunk` — claims the chunk the player is standing in for their team
- `/claim info` — prints the team that owns the current chunk

### /team (temporary)
- `/team create <name> <hex_color>` — creates a new team with the player as OWNER. Color is a hex string (e.g. `FF0000`)
- `/team delete <uuid>` — deletes a team by UUID, unclaims all its chunks, and broadcasts removals to clients