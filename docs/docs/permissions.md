# Permissions
Permissions use a bitfield (`int`) where each bit is a specific action. The `Permission` enum defines 17 flags (CLAIM_CHUNKS, BREAK_BLOCKS, PLACE_BLOCKS, etc.).

- `Permission.of(BREAK_BLOCKS, PLACE_BLOCKS)` — combine into a bitfield
- `perm.add(bits)` / `perm.remove(bits)` — flip bits on/off
- `perm.hasPermission(bits)` — check if a flag is set

### Roles
`Role` enum defines fixed roles with hardcoded default permissions:
- **OWNER** — full permissions
- **OFFICER** — management + most gameplay
- **MEMBER** — limited gameplay (building, survival)
- **DEFAULT** — no permissions, fallback for non-members

Each `TeamMember` stores a custom permission bitfield that can override role defaults. `PermissionManager` is the entry point — `playerHasBypass` grants ops (level 4) full access. `playerCanAccessChunk` is currently stubbed and needs wiring.
