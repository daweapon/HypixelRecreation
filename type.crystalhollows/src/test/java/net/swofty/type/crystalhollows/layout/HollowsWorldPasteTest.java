package net.swofty.type.crystalhollows.layout;

import net.hollowcube.polar.PolarLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.schematic.Schematic;
import net.swofty.type.skyblockgeneric.schematic.SchematicPaster;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class HollowsWorldPasteTest {
    private static final Path WORLD = Path.of("../configuration/world/hypixel_skyblock_crystal_hollows.polar");
    private static final Path STRUCTURES = Path.of("../configuration/skyblock/crystal_hollows/structures.yml");
    private static final Path SCHEMATICS = Path.of("../configuration/skyblock/crystal_hollows/schematics");

    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    @Test
    void everyStructureIsPastedExactlyOntoTheRealWorldForSeveralLayouts() throws IOException {
        assumeTrue(Files.exists(WORLD));
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.setChunkLoader(new PolarLoader(WORLD));
        List<StructureSpec> specs = StructureCatalogue.load(STRUCTURES);
        for (long seed = 1; seed <= 2; seed++) {
            CrystalHollowsLayout.generate(specs, seed);
            StructurePaster.Report report = StructurePaster.apply(instance, SchematicPaster.CALLER, specs, CrystalHollowsLayout.placements(), SCHEMATICS, seed).join();
            assertEquals(List.of(), report.failures(), "seed " + seed);
            assertEquals(0, report.unknownBlocks());
            for (StructureSpec spec : specs) {
                StructurePlacement placement = CrystalHollowsLayout.placements().get(spec.id());
                if (spec.schematic() == null) continue;
                assertTrue(placement != null, spec.id() + " seed " + seed);
                Schematic schematic = SchematicFiles.probe(SCHEMATICS, spec.schematic()).schematic();
                Random random = new Random(seed);
                int checked = 0, mismatched = 0;
                for (int i = 0; i < 3000; i++) {
                    int x = random.nextInt(schematic.width()), y = random.nextInt(schematic.height()), z = random.nextInt(schematic.length());
                    Block expected = schematic.blockAt(x, y, z);
                    Block actual = instance.getBlock(placement.origin().blockX() + x, placement.origin().blockY() + y, placement.origin().blockZ() + z);
                    checked++;
                    if (!expected.compare(actual, Block.Comparator.STATE)) mismatched++;
                }
                assertEquals(0, mismatched, spec.id() + " seed " + seed + " of " + checked);
            }
        }
    }
}
