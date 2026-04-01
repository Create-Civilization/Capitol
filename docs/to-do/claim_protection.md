# Claim Protection
Claims should protected from foreign interaction, by default it should be assumed that nobody is trusted, if there are any doubts
or issues, default to denying events.

## Permission Enforcement Status

### Administrative
- [ ] `CLAIM_CHUNKS`
- [ ] `UNCLAIM_CHUNKS`
- [ ] `FORCELOAD_CHUNKS`
- [ ] `INVITE_MEMBERS`
- [x] `KICK_MEMBERS` — `TeamCommand`
- [x] `MANAGE_ROLES` — `TeamCommand`
- [ ] `ASSIGN_ROLES`
- [x] `MANAGE_TEAM` — `TeamCommand`

### Block Protection
- [x] `BREAK_BLOCKS` — `LeftClickBlock` event
- [x] `PLACE_BLOCKS` — `RightClickBlock` event (BlockItem/BucketItem)
- [x] `INTERACT_BLOCKS` — `RightClickBlock` event (non-block items)
- [x] `OPEN_CONTAINERS` — `RightClickBlock` event (MenuProvider check)
- [ ] `USE_REDSTONE`
- [ ] `CROP_TRAMPLE`
- [ ] `FROST_WALKING`

### Entity Protection
- [x] `INTERACT_ENTITIES` — `EntityInteractSpecific` event
- [x] `KILL_ENTITIES` — `AttackEntityEvent` (non-hostile, non-player)
- [x] `KILL_HOSTILE` — `AttackEntityEvent` (Monster)
- [x] `PLAYER_ATTACK` — `AttackEntityEvent` (Player target)
- [ ] `ENTITY_GRIEFING`

### Item Protection
- [x] `USE_ITEMS` — `RightClickItem` event
- [x] `PICKUP_ITEMS` — `ItemEntityPickupEvent.Pre`
- [ ] `PICKUP_XP`
- [ ] `TOSS_ITEMS`
- [x] `MOB_LOOT` — `ItemEntityPickupEvent.Pre` (MobDeathDrop tag)
- [x] `PLAYER_DEATH_LOOT` — `ItemEntityPickupEvent.Pre` (DeathDrop tag)

### Redstone / Mechanical
- [ ] `TRIGGER_PRESSURE_PLATES`
- [ ] `TRIGGER_TRIPWIRES`
- [ ] `TRIGGER_BUTTONS_PROJECTILES`
- [ ] `TRIGGER_TARGETS_PROJECTILES`
- [ ] `DISPENSER_ACCESS`
- [ ] `PISTON_ACCESS`
- [ ] `FLUID_FLOW`

### Portal / Teleportation
- [ ] `USE_NETHER_PORTALS`
- [ ] `CHORUS_FRUIT_TELEPORT`

### Spawning
- [ ] `NATURAL_SPAWN_HOSTILE`
- [ ] `NATURAL_SPAWN_FRIENDLY`
- [ ] `SPAWNER_HOSTILE`
- [ ] `SPAWNER_FRIENDLY`

### Explosions / Environmental
- [ ] `BLOCK_EXPLOSIONS`
- [ ] `ENTITY_EXPLOSIONS`
- [ ] `FIRE_SPREAD`
- [ ] `LIGHTNING`
- [ ] `RAIDS`