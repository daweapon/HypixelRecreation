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

`--captures` cuts rooms from clientmod full captures of Hypixel Catacombs runs (`worlds/world_002`, `session.json` and
`streams/blocks.jsonl` + `streams/entities.jsonl` of each). Pass the Floor 1 captures first, then the Master Mode
Floor 7 ones (the folder name decides: `f1_...` or `m7_...`). It writes `hypixel/<room>.schem`, `hypixel_f1.json`
(rooms first seen on Floor 1) and `hypixel_m7.json` (the rest), and patches `m7.json`:

```sh
java tools/catacombs-rooms/ExtractRooms.java --captures F1_RUN1 F1_RUN2 M7_RUN1 M7_RUN2 M7_RUN3 M7_RUN4 CATALOGUE_DIR configuration/skyblock/dungeons/rooms
```

- Hypixel's grid starts at x = -200, z = -200 with 31-block rooms on a 32 pitch, growing toward positive X/Z, on every
  floor (F1 is 4 x 5, F7/M7 6 x 6; checked against the blocks and the Magical Map images). The whole dungeon's chunks
  arrive at the start of a run, so every room is in `world_002` even when nobody walked it.
- Rooms are found, not listed: every cell is named by the catalogue's core hash (`RoomCatalogue.java`), and a cell whose
  centre column no longer matches (Layers in M7 run 4 has a redstone block on its roof) joins the neighbouring room
  that is a cell short of its shape. Cells that are no room at all (the Bonzo arena reaches into the F1 grid) are left
  out. Doors are read from the seams: a door where the 3 x 4 doorway through both walls is open or holds a door block
  (coal, red terracotta, infested chiseled stone bricks). On the two F1 runs this matches the doors read off the maps.
- `CATALOGUE_DIR` holds `rooms.json` and `secretlocations.json`, the Dungeon Rooms Mod room catalogue (Copyright 2021
  Quantizr, GPL-3.0) as shipped by https://github.com/yourboykyle/SecretRoutes. Download them yourself; they are not
  in this repository. Only facts are taken from it: the room's name, kind, shape, crypt count and its secret and
  route-point coordinates. Chests, items, bats and Wither Essence become secret anchors; levers, superboom walls,
  stonk blocks, entrances and fairy souls go into `secretExtras` (the server uses levers and Superboom walls through `room_mechanisms.json`, below). Both keep the
  catalogue's own label (`"3 - Lever"` pairs with `"3 - Chest"`).
- Sightings of one room are compared block by block in the catalogue's room frame; ones that differ in more than 4% of
  their blocks are kept as variants (Blood, Entrance and Default each have two). Each variant is cut from one
  sighting (a Floor 1 one first, else the one whose mobs were seen best) but gets the doors of every sighting.
- A captured room the M7 practice map also has replaces the practice-map template under the same id when they differ
  in under 15% of their blocks, turned so it keeps the template's frame (`room_features.json` stays valid). Its
  `m7.json` entry and `m7/<id>.schem` are removed. The remaining `m7.json` rooms get the catalogue name, route points,
  secret labels (the catalogue secret of the same kind within two blocks) and crypt count.
- Hypixel sends secret chests and levers as block updates when a player comes near, so they are missing from the
  saved chunks; their states come from `blocks.jsonl`, and a chest never seen gets one facing an open side.
- Mob spawns (`CaptureMobs.java`) are each mob's first position in `entities.jsonl` with its name and star from the
  Hypixel name tag above it. Higher-floor mobs are stored as their Floor 1 counterpart (`mobId`) with the name seen
  (`seenAs`), so the rooms keep their Floor 1 stats. Where a room never showed a star the stars are guessed from the
  other rooms (`starredEstimated`).
- Crypt spots: real crypts are found by their 4x3 smooth stone slab lid; up to the catalogue's crypt count, the rest
  are estimated.
- The entrance variant with a spawn cave behind it (`start`) is cut from a run where it sits on the grid's top row, keeps
  the cave north of the grid like the practice-map Start did, and locks its rotation to 180 degrees.
- Special rooms (entrance, puzzle, miniboss, trap, fairy, blood) only get the door sockets seen in a run; the Blood
  room has a single one, at its back, opposite the portal frame.

## Doorways

A Hypixel door is a piece pasted over the seam and both rooms' two outer layers (5 wide, floor to 5 up); its look
differs per door, not per room. So:

- `--captures` also writes `configuration/skyblock/dungeons/door_pieces.json` (next to the rooms folder): every door
  piece of every run, turned so the door leads south, with the 3 x 4 passage emptied and the door block it was seen with
  (`PLAIN`, `WITHER`, `BLOOD`, `ENTRANCE`). The arena gives each door of a layout one of these.
- A room is cut with its closed walls: where the chosen sighting had a door, the door piece's room side is replaced by
  the same wall from another sighting of the room where that socket was shut (`"closedWall": "captured"`), else the wall
  beside the doorway is carried across it (`"estimated"`; the inner layer stays a plain recess). `"seen"` means the cut
  sighting itself had no door there. Sockets, spawns, stand points and crypts are still worked out on the room as read
  before (only the 3 x 4 hole filled), so they do not move.
- Practice-map rooms (Quiz, Market, Wizard, Well) get the estimated closed wall at every M7 door.

## Levers and Superboom walls

```sh
java tools/catacombs-rooms/ExtractRooms.java --mechanisms configuration/skyblock/dungeons/rooms configuration/skyblock/dungeons/room_mechanisms.json
```

reads every template and its catalogue `secretExtras` and writes, in the template's frame:

- `superbooms`: the cracked stone brick plug at each catalogue Superboom point (cracked bricks that touch at least two
  others; a box that is at least three quarters cracked is taken whole, as M7 run 3 removed a full 2 x 4 x 2 plug).
- `levers`: the lever block, its paired secrets (same catalogue number) and the blocks a pull removes. `capture` comes
  from a captured pull (`Mechanisms.CAPTURED`), `checked` from a hand-checked list (`Mechanisms.CHECKED`), `estimated`
  from the room's shape: the closed air pocket holding the paired secret (else the nearest iron-bar style gate) and the
  one-block plug between it and the open room. `freesSecret` marks levers whose plug shuts the secret's own pocket.
  `none` means no such plug was found (or only plain wall away from the secret, which is not guessed at) and the lever
  opens nothing.

`ExportRegion.java` saves any box of a capture as a schematic (used for the Bonzo arena).
