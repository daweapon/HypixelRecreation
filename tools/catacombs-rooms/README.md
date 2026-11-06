# Catacombs room extraction

`ExtractRooms.java` cuts the 16 real Catacombs rooms out of the MIT-licensed M7 practice map
(https://github.com/strad-dev/m7, licence in `configuration/skyblock/dungeons/rooms/LICENSE-m7.txt`) and writes:

- `configuration/skyblock/dungeons/rooms/m7/<room>.schem`: one Sponge v2 schematic per room (gzip NBT, the format
  `SchematicReader` reads), turned so its cells match the base `RoomShape`, cropped to its cells and internal seams,
  every non-air Y in that footprint.
- `configuration/skyblock/dungeons/rooms/m7.json`: the room templates (shape, kind, secrets, door sockets, estimated
  mob spawns, crypts and stand points).

No dependencies; run it from the repository root with the unzipped map world (Paper 26.x layout):

```sh
java tools/catacombs-rooms/ExtractRooms.java WORLD/dimensions/minecraft/overworld/region configuration/skyblock/dungeons/rooms
```

Room cells, Y ranges, secrets and the 15 doors are copied from the map's `instructions/clear/Rooms.java` into
`M7Rooms.java`. What the tool changes in the blocks:

- M7 command blocks become the block next to them; a sign with the map author's own text is removed; block entities
  keep only skull profiles, sign text and banner patterns.
- Every secret chest gets a chest block (trapped for the Mimic) facing an open side; Wither Essence keeps its wither
  skull.
- The M7 door holes in the outer walls are closed with the wall beside them. The arena cuts a 3 by 4 opening wherever
  a layout puts a door.
- Start keeps the 13 blocks behind its cell (Z -213..-201, the spawn cave and portal), and its template locks
  rotation to 180 degrees so that part points out of the grid.

Estimated, not from the map: door sockets other than the M7 doors (a wall counts when the floor behind it connects to
the M7 doors by walking), mob spawns (spread over open floor near door height), crypt spots and stand points. The Ice
Fill reward chests are left out because they are not scored secrets.

## Rooms from captured Hypixel runs

`--captures` cuts rooms from two clientmod full captures of Hypixel Floor 1 runs (`worlds/world_002` plus
`streams/blocks.jsonl` of each) and writes `hypixel/<room>.schem` and `hypixel_f1.json`:

```sh
java tools/catacombs-rooms/ExtractRooms.java --captures RUN1_CAPTURE RUN2_CAPTURE CATALOGUE_DIR configuration/skyblock/dungeons/rooms
```

- Hypixel's grid starts at x = -200, z = -200 with 31-block rooms on a 32 pitch, growing toward positive X/Z (checked
  against the captured blocks and the Magical Map images). The cells and doors of each run (`CaptureRooms.java`) were
  read off the final map image and confirmed by the blocks in the one-block seams between cells.
- `CATALOGUE_DIR` holds `rooms.json` and `secretlocations.json`, the Dungeon Rooms Mod room catalogue (Copyright 2021
  Quantizr, GPL-3.0) as shipped by https://github.com/yourboykyle/SecretRoutes. Download them yourself; they are not
  in this repository. `RoomCatalogue.java` names each room the way that mod does (a hash of the block column at the
  cell centre, the blue terracotta marker for the corner and rotation) and takes from it only facts: the room's name,
  kind, shape, crypt count and its secret and route-point coordinates. Chests, items, bats and Wither Essence become
  secret anchors; levers, superboom walls, stonk blocks, entrances and fairy souls go into `secretExtras`, which the
  server does not read yet.
- Hypixel sends secret chests and levers as block updates when a player comes near, so they are missing from the
  saved chunks; their states come from `blocks.jsonl`, and a chest never seen gets one facing an open side.
- Mob spawns (`CaptureSpawns.java`) are each mob's first position in `entities.jsonl` with its type and star from the
  name tag above it. Where a room never showed a star, stars are guessed from the other rooms (`starredEstimated`).
- Crypt spots: real crypts are found by their 4x3 smooth stone slab lid (the tomb stands on top of it); up to the
  catalogue's crypt count, the rest are estimated.
- Rooms the M7 map already has (Hallway, Fairy, Blood) are not cut again: `m7.json` is patched in place with the
  captured Hallway spawns, each room's `catalogueName`, its route points, and crypt spots trimmed to the catalogue
  count. Run `--captures` again after re-running the M7 extraction. The Entrance is cut as a second variant because the
  M7 one differs (cave annex).

`ExportRegion.java` saves any box of a capture as a schematic (used for the Bonzo arena).
