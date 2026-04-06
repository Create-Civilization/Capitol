# Capitol
A Create Civilization project focused on improving chunk claiming systems, focusing on systems management and mod parity.

## Goals:
- Parity with many popular mods
- Modularized systems
- Easy development (API, Testing, deployment)
- Secure claim system

## Development Goals:
- [Clear documentation](docs/documentation.md)
- Extensive Production tools for testing (Unit tests, CI/CD tools, etc)
- Modular systems
- Fast development, functionality > polish


## System modules:
To keep things robust, modular and fast the Capitol mod should be easily dividable in modular sections such as:
- Database: JSON, SQL, etc
- Claim protection: Prevent unlawful actions on claimed chunks
- Permission manager: allow the creation of admins, moderators and other roles for maintenance

## Current Status
16 of 43 permissions are currently enforced. See the full [implementation checklist](docs/to-do/claim_protection.md) and [permission docs](docs/docs/permissions.md) for details.