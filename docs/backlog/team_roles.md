# Team Roles
Teams have fixed roles (`OWNER`, `OFFICER`, `MEMBER`) with hardcoded default permissions for now. Each role stores its default permission bitfield, applied at join time.

- [ ] Config-driven role permissions (replace hardcoded defaults in `Role` enum)
- [ ] Custom roles — teams creating their own roles with arbitrary permissions