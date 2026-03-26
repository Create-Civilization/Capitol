# Permission manager
Permissions are stored as a bitfield (`int`) per player in `team_members`. The `Permission` enum defines each flag and provides helpers:

- `Permission.of(...)` — combine permissions into a bitfield
- `Permission.add(bits, perm)` / `Permission.remove(bits, perm)` — set/clear a bit
- `Permission.hasPermission(bits)` — check if a permission is present

Default permissions per role are defined in the `Role` enum and applied when a player joins a team. A `DEFAULT` role (no permissions) is used as the fallback for non-members.

- [ ] Config-driven default permissions per role (see `TODO` in `Role.getDefaultPerms()`)