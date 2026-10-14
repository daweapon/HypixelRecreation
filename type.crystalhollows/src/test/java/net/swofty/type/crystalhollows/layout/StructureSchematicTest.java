package net.swofty.type.crystalhollows.layout;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.schematic.SchematicPaster;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureSchematicTest {
    @BeforeAll
    static void init() {
        MinecraftServer.init();
    }

    private static void write(Path file, String block, int w, int h, int l) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        for (int i = 0; i < w * h * l; i++) data.write(0);
        CompoundBinaryTag root = CompoundBinaryTag.builder().putInt("Version", 2).putInt("DataVersion", 3700)
                .putShort("Width", (short) w).putShort("Height", (short) h).putShort("Length", (short) l)
                .put("Palette", CompoundBinaryTag.builder().putInt(block, 0).build())
                .putByteArray("BlockData", data.toByteArray()).build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BinaryTagIO.writer().write(root, out, BinaryTagIO.Compression.GZIP);
        Files.write(file, out.toByteArray());
    }

    private static StructureSpec spec(String schematic, String patch, StructureSite site) {
        return new StructureSpec("temple", "Temple", RegionType.JUNGLE, false, 10, List.of(), Map.of(), schematic, site, patch, List.of());
    }

    @Test
    void schematicFieldsRoundTripAndShippedFileStaysUnchanged(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("structures.yml");
        Files.writeString(file, """
                structures:
                  - id: temple
                    name: "Temple"
                    region: JUNGLE_TEMPLE
                    optional: false
                    clearance: 40
                    schematic: "temple.schem"
                    empty_patch: "temple_empty.schem"
                    rotations: [0, 90]
                    site: {min: {x: 1, y: 2, z: 3}, max: {x: 11, y: 12, z: 13}}
                    candidates: []
                    anchors: {}
                """);
        StructureSpec spec = StructureCatalogue.load(file).getFirst();
        assertEquals("temple.schem", spec.schematic());
        assertEquals("temple_empty.schem", spec.emptyPatch());
        assertEquals(List.of(0, 90), spec.rotations());
        assertEquals(new StructureSite(1, 2, 3, 11, 12, 13), spec.site());
        assertEquals(11, spec.site().width());
        assertEquals(Files.readString(file), StructureFile.read(file).render());
        assertEquals("jungle_temple", StructureCatalogue.load(Path.of("../configuration/skyblock/crystal_hollows/structures.yml")).getFirst().schematic());
    }

    @Test
    void shippedSchematicsAreReadableAndMatchTheirSites() {
        List<StructureSpec> specs = StructureCatalogue.load(Path.of("../configuration/skyblock/crystal_hollows/structures.yml"));
        Path directory = Path.of("../configuration/skyblock/crystal_hollows/schematics");
        long withSchematic = specs.stream().filter(spec -> spec.schematic() != null).count();
        assertTrue(withSchematic >= 7);
        for (StructureSpec spec : specs) {
            if (spec.schematic() == null) continue;
            SchematicFiles.Probe probe = SchematicFiles.probe(directory, spec.schematic());
            assertTrue(probe.readable(), spec.id() + ": " + probe.error());
            assertEquals(spec.site().width(), probe.schematic().width(), spec.id());
            assertEquals(spec.site().height(), probe.schematic().height(), spec.id());
            assertEquals(spec.site().length(), probe.schematic().length(), spec.id());
            assertEquals(0, probe.schematic().unknownBlocks(), spec.id());
            assertTrue(spec.candidates().size() >= 2 || spec.id().equals("mines_of_divan"), spec.id());
        }
    }

    @Test
    void validatorReportsMissingUnreadableUnknownAndPatchlessSites(@TempDir Path dir) throws IOException {
        write(dir.resolve("good.schem"), "minecraft:stone", 2, 1, 1);
        write(dir.resolve("odd.schem"), "minecraft:not_a_block", 2, 1, 1);
        Files.writeString(dir.resolve("broken.schem"), "nope");
        StructureSite site = new StructureSite(0, 0, 0, 4, 4, 4);
        List<HollowsValidator.Issue> issues = HollowsValidator.validateSchematics(List.of(
                spec("missing", null, null), spec("broken", null, null), spec("odd", null, null),
                spec("good", null, site), spec("good", "good", site)), dir);
        List<String> messages = issues.stream().map(HollowsValidator.Issue::message).toList();
        assertTrue(messages.stream().anyMatch(m -> m.contains("missing") && m.contains("not found")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("broken") && m.contains("unreadable")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("odd") && m.contains("2 unknown blocks")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("no empty_patch")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("but the site is 5x5x5")));
    }

    @Test
    void fileNamesCannotEscapeTheDirectory(@TempDir Path dir) {
        assertNull(SchematicFiles.resolve(dir, "../x.schem"));
        assertNotNull(SchematicFiles.resolve(dir, "x"));
        assertEquals(dir.toAbsolutePath().normalize().resolve("x.schem"), SchematicFiles.resolve(dir, "x"));
    }

    @Test
    void pasterPlacesStructuresErasesUnusedSitesAndRestoresOnReapply(@TempDir Path dir) throws IOException {
        write(dir.resolve("temple.schem"), "minecraft:stone", 2, 1, 2);
        write(dir.resolve("empty.schem"), "minecraft:air", 2, 1, 2);
        Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
        instance.loadChunk(0, 0).join();
        for (int x = 0; x < 2; x++) for (int z = 0; z < 2; z++) instance.setBlock(x, 70, z, Block.GOLD_BLOCK);
        for (int x = 8; x < 10; x++) for (int z = 8; z < 10; z++) instance.setBlock(x, 70, z, Block.DIRT);

        StructureSite site = new StructureSite(0, 70, 0, 1, 70, 1);
        StructureSpec spec = spec("temple", "empty", site);
        StructurePlacement placement = new StructurePlacement(spec, new Pos(8, 70, 8));
        StructurePaster.Report report = StructurePaster.apply(instance, SchematicPaster.CALLER, List.of(spec),
                Map.of("temple", placement), dir, 1L).join();
        assertEquals(List.of("temple"), report.pasted());
        assertEquals(List.of("temple"), report.erased());
        assertTrue(report.failures().isEmpty());
        assertEquals(Block.AIR, instance.getBlock(0, 70, 0));
        assertEquals(Block.STONE, instance.getBlock(9, 70, 9));

        StructurePlacement atSite = new StructurePlacement(spec, new Pos(0, 70, 0));
        StructurePaster.Report second = StructurePaster.apply(instance, SchematicPaster.CALLER, List.of(spec),
                Map.of("temple", atSite), dir, 1L).join();
        assertTrue(second.erased().isEmpty());
        assertEquals(Block.DIRT, instance.getBlock(9, 70, 9));
        assertEquals(Block.STONE, instance.getBlock(1, 70, 1));

        StructurePaster.apply(instance, SchematicPaster.CALLER, List.of(spec(null, null, site)), Map.of(), dir, 1L).join();
        assertEquals(Block.GOLD_BLOCK, instance.getBlock(1, 70, 1));
    }
}
