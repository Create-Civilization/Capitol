# Permission manager
Permissions are stored as a bitfield (`long`) per player in `team_members`. The `Permission` enum defines 43 flags and provides instance helpers:

- `Permission.of(...)` — combine permissions into a bitfield
- `perm.add(bits)` / `perm.remove(bits)` — set/clear a bit
- `perm.hasPermission(bits)` — check if a permission is present

Default permissions per role are defined in the `Role` enum and applied when a player joins a team. A `DEFAULT` role (no permissions) is used as the fallback for non-members.

`PermissionManager` is the entry point for chunk access checks. `playerHasBypass` grants full access to operators (permission level 4).

- [ ] `playerCanAccessChunk` — needs `ClaimManager` integration and full permission lookup to be wired up
- [ ] Config-driven default permissions per role (see `TODO` in `Role.getDefaultPerms()`)