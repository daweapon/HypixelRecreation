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
