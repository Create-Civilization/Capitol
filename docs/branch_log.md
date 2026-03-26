# Branch log
To keep branches understandable, create a small log underneath about the purpose of your branch and what it hopes to accomplish and implement:

## Branch 1.21.1-rc:
- The 1.21.1 Neoforge **c**ontinuation version of the [**r**ewrite branch](https://github.com/Create-Civilization/Capitol/tree/1.21.1-rewrite) (thus the name, **r**ewrite **c**ontinuation branch)
- Main branch for all further rewrites, hopefully to act as a replacement to the [1.21.1 branch](https://github.com/Create-Civilization/Capitol/tree/1.21.1)
- Create a documentation standard for all those wishing to add onto Capitol

## Branch 1.21.1-rc-database:
- Implements the SQLite database layer via `CapitolDatabase` and `DatabaseManager` (WAL mode, foreign keys, cascade deletes)
- Introduces a bitfield-based permission system (`Permission` enum) and fixed roles with default permissions (`Role` enum)
- Rewrites `Team` with a builder pattern, color (ARGB), UUID, and SQL `fromResultSet` support
- Adds `TeamCommand` (create team with hex color) and `Claim` (claim/info) as temporary testing commands
- `PermissionManager.playerCanAccessChunk` is stubbed — wiring to `ClaimManager` is still TODO