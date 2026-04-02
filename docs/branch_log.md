# Branch log
To keep branches understandable, create a small log underneath about the purpose of your branch and what it hopes to accomplish and implement:

## Branch 1.21.1-rc:
- The 1.21.1 Neoforge **c**ontinuation version of the [**r**ewrite branch](https://github.com/Create-Civilization/Capitol/tree/1.21.1-rewrite) (thus the name, **r**ewrite **c**ontinuation branch)
- Main branch for all further rewrites, hopefully to act as a replacement to the [1.21.1 branch](https://github.com/Create-Civilization/Capitol/tree/1.21.1)
- Create a documentation standard for all those wishing to add onto Capitol

## Branch 1.21.1-rc-database (DELETED - Merged To 1.21.1-rc):
- Implements the SQLite database layer via `CapitolDatabase` and `DatabaseManager` (WAL mode, foreign keys, cascade deletes)
- Introduces a bitfield-based permission system (`Permission` enum) and fixed roles with default permissions (`Role` enum)
- Rewrites `Team` with a builder pattern, color (ARGB), UUID, `StreamCodec`, and SQL `fromResultSet` support
- Adds `TeamCommand` (create team with hex color) and `Claim` (claim/info) as temporary testing commands — **to be removed before merge**
- `PermissionManager.playerCanAccessChunk` is stubbed — wiring to `ClaimManager` is still TODO

## Branch 1.21.1-rc-visuals (DELETED - Merged to 1.21.1-rc-databse):
- Networking layer: `BorderPacket` / `BorderRemovePacket` send claimed chunk data to clients; `ClientClaimCache` stores it client-side
- `BorderRenderer` renders a **temporary** placeholder wireframe over claimed chunks — to be replaced with a proximity-fade wall effect
- Client cache cleared on disconnect; chunk load/unload events keep cache in sync for already-loaded chunks
- C2S request packet for syncing chunks on player join still TODO (bulk send intentionally avoided due to scale)

## Branch 1.21.1-rc-protection (DELETED - Merged to 1.21.1-rc-commands):
- Upgrades permission bitfield from `int` to `long` (64 permissions)
- Adds `PlayerInteractionEvents` for server-side claim protection via NeoForge events
- Enforces: block break/place, block interaction, container access (MenuProvider check), entity interaction, entity killing (hostile/friendly/player split), and item use
- 13 of 43 permissions currently enforced — see `docs/to-do/claim_protection.md` for full checklist
- Still TODO: redstone, environmental, spawning, portal, and remaining item permissions

## Branch 1.21.1-rc-commands:
- Rewrites `TeamCommand` with full subcommand structure: `create`, `delete` (with clickable confirmation prompt), and `kick` (with player name autocomplete)
- `team create` now accepts `<name> <tag> <color> [description]` instead of just name and color
- Permission-gated: `delete` requires `MANAGE_TEAM`, `kick` requires `KICK_MEMBERS`
- `ClaimCommand` carries over `chunk` and `info` subcommands from the database branch
- Updated command documentation in `docs/docs/commands.md`
- Still TODO: `team invite`, `team manage`, `team info` subcommands