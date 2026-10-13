# Crystal Hollows world conversion

`ConvertAnvilToPolar.java` converts a client-mod `/fullcapture` world folder (vanilla Anvil, `region/*.mca`) into the Polar file the server loads as `CustomWorlds.SKYBLOCK_CRYSTAL_HOLLOWS` (`configuration/world/hypixel_skyblock_crystal_hollows.polar`).

It uses the same import as the client mod (`AnvilPolar.anvilToPolar` with `ChunkSelector.all()`), pads or keeps sections at -4..19 so the file matches the other world files (Polar version 7, ZSTD, no heightmaps, no light, no user data), and keeps all block entities and the captured biome palettes. It then prints chunk coverage, block entity counts, biomes and the non-air block bounds, and optionally writes `coverage.txt` and `coverage.png` (green = chunk with blocks, grey = air-only chunk, dark = missing).

Run from the repository root with the built core jar (it bundles Minestom, Polar and zstd):

```sh
java --enable-native-access=ALL-UNNAMED -cp loader/build/libs/HypixelCore.jar \
  tools/crystalhollows/ConvertAnvilToPolar.java \
  CAPTURE/worlds/world_001 configuration/world/hypixel_skyblock_crystal_hollows.polar REPORT_DIR
```

`REPORT_DIR` is optional. `configuration/world` is git-ignored, so the output file is not committed and must be copied to the server hosts like the other world files.

Capture 2026-10-13 (`CrystalHallows1`): 1631 chunks, 1220 block entities, non-air bounds x 272..824, y 30..189, z 201..824. The capture lacks the westernmost strip (chunk x 12..16, block x 202..271) and about 76 other chunks of the real 202..823 square.
