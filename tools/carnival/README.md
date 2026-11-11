# Carnival world paste

`CarnivalWorldPaste.java` pastes the real Hypixel Carnival into our Hub world (`configuration/world/hypixel_skyblock_hub.polar`). It replaces only the blocks and block entities inside the given boxes. The rest of our Hub is left alone.

`carnival_2026_11_10.polar` holds the 24 chunks (x -128..-65, z -16..79) cut from the user's capture `Carnival_2026-11-10_20-03-28`. The capture itself is git-ignored, so this file is the source of truth. Force-add it if you replace it (`git add -f`).

Run from the repository root with the built core jar:

```sh
J="java --enable-native-access=ALL-UNNAMED -cp loader/build/libs/HypixelCore.jar tools/carnival/CarnivalWorldPaste.java"
cp configuration/world/hypixel_skyblock_hub.polar configuration/world/hypixel_skyblock_hub.polar.pre-carnival.bak
$J paste configuration/world/hypixel_skyblock_hub.polar.pre-carnival.bak tools/carnival/carnival_2026_11_10.polar -122,62,10,-67,100,64 /tmp/hub_a.polar
$J paste /tmp/hub_a.polar tools/carnival/carnival_2026_11_10.polar -122,62,-2,-78,100,9 configuration/world/hypixel_skyblock_hub.polar
```

There are two boxes because the north-east corner (x > -78, z < 10) holds trees and underground stone that differ from ours but aren't part of the Carnival. Run 2026-11-10: 11,781 + 696 blocks changed and 8 block entities pasted (6 chests, a jukebox and a player head). No block state was unknown to Minestom.

To undo, copy the `.pre-carnival.bak` file back.

The other modes help you choose the boxes:
- `diff` prints a per-column difference map.
- `render` writes top-down PNGs of both worlds.
- `entities` lists the block entities in a box.
- `column` prints one column from both worlds.
- `extract` cuts the chunks around a box out of an Anvil capture.
