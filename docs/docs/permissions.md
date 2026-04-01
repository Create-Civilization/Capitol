# Permissions
Permissions use a bitfield (`long`) where each bit is a specific action. The `Permission` enum defines 43 flags across 8 categories: Administrative, Block Protection, Entity Protection, Item Protection, Redstone/Mechanical, Portal/Teleportation, Spawning, and Explosions/Environmental.

- `Permission.of(BREAK_BLOCKS, PLACE_BLOCKS)` — combine into a bitfield
- `perm.add(bits)` / `perm.remove(bits)` — set/clear a bit
- `perm.toggle(bits)` — flip a bit
- `perm.hasPermission(bits)` — check if a flag is set

### Roles
`TeamRole` defines roles with default permissions:
- **OWNER** — full permissions (all bits set)
- **DEFAULT** — no permissions (fallback for non-members)

Each `TeamMember` stores a custom permission bitfield that can override role defaults. `PermissionManager` is the entry point — `playerHasBypass` grants ops (level 4) full access. `playerHasPermission` checks a player's permission for a specific chunk.

### Enforcement
Permission checks are enforced in `PlayerInteractionEvents` on the server side. See [claim_protection.md](../to-do/claim_protection.md) for the full implementation status checklist.
