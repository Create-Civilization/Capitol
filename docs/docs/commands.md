# Commands

All commands are registered under the `/capitol` prefix via `CapitolCommands`.

---

## /capitol team

Team management commands. Most subcommands require the player to be in a team already.

### Implemented

| Command | Syntax | Description | Permission |
|---------|--------|-------------|------------|
| create | `/capitol team create <name> <tag> <color> [description]` | Creates a new team with the player as owner. Color is a hex string (e.g. `FF0000`). Description is optional. | None (must not already be in a team) |
| delete | `/capitol team delete` | Prompts a confirmation dialog with clickable [YES]/[NO] buttons in chat. | `MANAGE_TEAM` |
| kick | `/capitol team kick <player>` | Kicks a player from your team. Provides autocomplete suggestions for team member names. | `KICK_MEMBERS` |

- `confirmdelete` is an internal subcommand triggered by the [YES] button in the delete prompt.

### TODO

| Command | Syntax | Description | Permission |
|---------|--------|-------------|------------|
| invite | `/capitol team invite <player>` | Invite a player to join your team. | `INVITE_MEMBERS` |
| manage | `/capitol team manage ...` | Manage team settings (rename, roles, etc.). | `MANAGE_TEAM` |
| info | `/capitol team info` | Display information about your current team (name, tag, color, members, etc.). | None |

---

## /capitol claim

Land claiming commands. The player must be in a team to use these.

### Implemented

| Command | Syntax | Description | Permission |
|---------|--------|-------------|------------|
| chunk | `/capitol claim chunk` | Claims the chunk the player is standing in for their team. Broadcasts the claim to nearby players. | None (must be in a team) |
| info | `/capitol claim info` | Prints the team that owns the chunk the player is standing in, or "No Claim In This Chunk" if unclaimed. | None |

---

## Permissions

Commands are gated by a bitfield permission system defined in `Permission.java`. Each team member has a role with a permission bitfield. The relevant permissions for commands are:

| Permission | Description |
|------------|-------------|
| `MANAGE_TEAM` | Required for team deletion, renaming, and settings |
| `KICK_MEMBERS` | Required to kick players from the team |
| `INVITE_MEMBERS` | Required to invite players to the team |
| `CLAIM_CHUNKS` | Claim land for the team |
| `UNCLAIM_CHUNKS` | Unclaim team land |

See `Permission.java` for the full list of gameplay permissions (block interaction, containers, entities, etc.).
