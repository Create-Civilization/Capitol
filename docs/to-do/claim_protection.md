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
- [x] `ASSIGN_ROLES` — `TeamCommand`
- [x] `MANAGE_TEAM` — `TeamCommand`

### Block Protection
- [x] `BREAK_BLOCKS` — `LeftClickBlock` event
- [x] `PLACE_BLOCKS` — `RightClickBlock` event (BlockItem/BucketItem)
- [x] `INTERACT_BLOCKS` — `RightClickBlock` event (non-block items)
- [x] `OPEN_CONTAINERS` — `RightClickBlock` event (MenuProvider check)
- [x] `CROP_TRAMPLE` — `FarmlandTrampleEvent`
- [x] `FROST_WALKING` — `EntityPlaceEvent` (FrostedIceBlock check)

### Entity Protection
- [x] `INTERACT_ENTITIES` — `EntityInteractSpecific` event
- [x] `KILL_ENTITIES` — `AttackEntityEvent` (non-hostile, non-player)
- [x] `KILL_HOSTILE` — `AttackEntityEvent` (Monster)
- [x] `PLAYER_ATTACK` — `AttackEntityEvent` (Player target)

### Item Protection
- [x] `USE_ITEMS` — `RightClickItem` event
- [x] `PICKUP_ITEMS` — `ItemEntityPickupEvent.Pre`
- [x] `PICKUP_XP` — `PlayerXpEvent.PickupXp`
- [x] `TOSS_ITEMS` — `ItemTossEvent`
- [x] `MOB_LOOT` — `ItemEntityPickupEvent.Pre` (MobDeathDrop tag)
- [x] `PLAYER_DEATH_LOOT` — `ItemEntityPickupEvent.Pre` (DeathDrop tag)

### Redstone
- [x] `INTERACT_REDSTONE` — `RightClickBlock` event (isSignalSource check)

### Portal / Teleportation
- [x] `USE_NETHER_PORTALS` — `EntityTravelToDimensionEvent`
- [x] `CHORUS_FRUIT_TELEPORT` — `EntityTeleportEvent.ChorusFruit`