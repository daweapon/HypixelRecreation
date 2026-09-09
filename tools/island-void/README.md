# Private island void cleanup

The original `hypixel_skyblock_island_template.polar` contains 1,817 chunks, including normal overworld terrain and stray underground blocks. The two starter islands occupy four nonempty chunks inside chunk X -1 through 0 and Z 0 through 2 (block X -16 through 15, Z 0 through 47). The cleaned template retains those six chunks, including two empty chunks.

`IslandWorldStorage.templateWorld()` restricts new islands to this starter area. This restriction applies only to the template; saved islands can retain player builds anywhere. If the starter island layout changes, update these template bounds.

`CleanIslandWorld.java` repairs an existing export by comparing it with the **original, unmodified** template. It retains every starter-area chunk and every chunk absent from the original template. It removes outside chunks only if all block states, block entities, and chunk user data match the original. Block-state names are normalized through Minestom. Any modified outside-template terrain chunk aborts the operation for manual review. Lighting, biome and heightmap differences do not count as player block changes.

Run from the repository root, with a built core jar:

```sh
java -cp loader/build/libs/HypixelCore.jar tools/island-void/CleanIslandWorld.java ORIGINAL_TEMPLATE INPUT_WORLD OUTPUT_WORLD
```

The tool only writes the output file. For live repair, stop the island server so its shutdown save completes, back up the Redis island hashes and original template, export fresh island data, prepare and validate every output, then replace only the `data` field. Preserve all other island fields. Restart the server after replacing data; verify startup and the saved world contents. Never use an old export while a server can save newer player edits.

Verify the built server's actual template loader and byte-for-byte preservation of each starter chunk:

```sh
java -cp loader/build/libs/HypixelCore.jar tools/island-void/VerifyVoidTemplate.java
```
