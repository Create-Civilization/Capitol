# Commands

All commands are registered under the `/capitol` prefix via `CapitolCommands`.

---

## /capitol help

Prints a formatted list of all available commands to the player's chat.

---

## /capitol team

Team management commands. Most subcommands require the player to be in a team already.

| Command    | Syntax                                                                 | Description                                                                                                                                            | Permission                           |
|------------|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| create     | `/capitol team create <name> <tag> <color> [description]`              | Creates a new team with the player as owner. Color accepts named colors (e.g. `red`, `gold`) or a hex string (e.g. `FF0000`). Description is optional. | None (must not already be in a team) |
| info       | `/capitol team info`                                                   | Displays the team's name, tag, color, description, and claim count.                                                                                    | None                                 |
| invite     | `/capitol team invite <player>`                                        | Sends a team invite to an online player. The recipient accepts or denies via `/capitol invites`.                                                       | `INVITE_MEMBERS`                     |
| kick       | `/capitol team kick <player>`                                          | Kicks a player from your team. Provides autocomplete suggestions for team member names.                                                                | `KICK_MEMBERS`                       |
| leave      | `/capitol team leave`                                                  | Leaves your current team. The team owner must disband instead of leaving.                                                                              | None                                 |
| disband    | `/capitol team disband`                                                | Prompts a confirmation dialog with a clickable [YES] button in chat. Removes the team and broadcasts `S2CChunkRemove` for all claimed chunks.          | `MANAGE_TEAM`                        |
| role       | `/capitol team role <name> create\|remove\|rename\|assign\|permission` | Manage team roles. See `TeamRoleCommand`.                                                                                                              | Varies                               |
| protection | `/capitol team protection <name>`                                      | Toggles a team-level protection setting by name. See `TeamProtectionCommand`.                                                                          | `MANAGE_TEAM`                        |
| player     | `/capitol team player <player> permission <perm>`                      | Toggles an individual permission override for a team member. `reset` clears all overrides.                                                             | `MANAGE_ROLES`                       |
| forceload  | `/capitol team forceload ...`                                          | Manage forceloaded chunks for the team. See `TeamForceloadCommand`.                                                                                    | `FORCELOAD_CHUNKS`                   |

- `confirm_disband` is an internal subcommand triggered by the [YES] button in the disband prompt.

---

## /capitol claim

Land claiming commands. The player must be in a team to use these.

| Command        | Syntax                          | Description                                                                                              | Permission     |
|----------------|---------------------------------|----------------------------------------------------------------------------------------------------------|----------------|
| chunk          | `/capitol claim chunk`          | Claims the chunk the player is standing in for their team.                                               | `CLAIM_CHUNKS` |
| sub_level      | `/capitol claim sub_level`      | Claims the Create sub-level the player is currently inside. Only registered if Sable is loaded.          | `CLAIM_CHUNKS` |
| info           | `/capitol claim info`           | Prints the team that owns the chunk the player is standing in, or "No Claim In This Chunk" if unclaimed. | None           |
| info sub_level | `/capitol claim info sub_level` | Prints the team that owns the current sub-level.                                                         | None           |

---

## /capitol unclaim

| Command   | Syntax                       | Description                                                                                       | Permission       |
|-----------|------------------------------|---------------------------------------------------------------------------------------------------|------------------|
| chunk     | `/capitol unclaim chunk`     | Unclaims the chunk the player is standing in. Must be owned by the player's team.                 | `UNCLAIM_CHUNKS` |
| sub_level | `/capitol unclaim sub_level` | Unclaims the Create sub-level the player is currently inside. Only registered if Sable is loaded. | `UNCLAIM_CHUNKS` |

---

## /capitol invites

| Command | Syntax                           | Description                                                                                            | Permission |
|---------|----------------------------------|--------------------------------------------------------------------------------------------------------|------------|
| accept  | `/capitol invites <team> accept` | Accepts a pending team invite. Adds the player at the team's default role and notifies online members. | None       |
| deny    | `/capitol invites <team> deny`   | Denies a pending team invite.                                                                          | None       |

The `<team>` argument autocompletes to the names of teams that have invited the player.

---

## Permissions

Commands are gated by a bitfield permission system defined in `Permission.java`. Each team member has a role with a permission bitfield. The relevant permissions for commands are:

| Permission         | Description                                               |
|--------------------|-----------------------------------------------------------|
| `MANAGE_TEAM`      | Required for disbanding and protection settings           |
| `KICK_MEMBERS`     | Required to kick players from the team                    |
| `INVITE_MEMBERS`   | Required to invite players to the team                    |
| `MANAGE_ROLES`     | Required to toggle individual player permission overrides |
| `ASSIGN_ROLES`     | Required to assign roles to members                       |
| `CLAIM_CHUNKS`     | Claim land for the team                                   |
| `UNCLAIM_CHUNKS`   | Unclaim team land                                         |
| `FORCELOAD_CHUNKS` | Manage forceloaded chunks                                 |

See `Permission.java` for the full list of gameplay permissions (block interaction, containers, entities, etc.).
