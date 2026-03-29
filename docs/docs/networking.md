# Networking
Chunk claim data is synced between server and client via NeoForge's networking layer. `CapitolNetworking` registers all packet types and routes them to their handlers.

### Packets
- **S2CChunkData** — server tells client a chunk is claimed (sends ChunkPos + Team data)
- **S2CChunkRemove** — server tells client a chunk was unclaimed
- **C2SChunkRequest** — client asks server for a chunk's ownership info

### Client Side
- `ClientClaimCache` stores a `HashMap<ChunkPos, Team>` of all known claims
- `ChunkEvents` keeps the cache in sync: sends a `C2SChunkRequest` on chunk load, removes from cache on chunk unload, clears everything on disconnect
- `ClientPayloadHandler` receives S2C packets and updates the cache
- `ServerPayloadHandler` receives C2S requests and responds with the chunk's owner from the database