package net.swofty.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.skyblockgeneric.crystalhollows.NucleusCrystal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HollowsToolingTest {
    private static final Path SHIPPED = Path.of("src/test/resources/crystal_hollows");
    private static final Path REAL = Path.of("../configuration/skyblock/crystal_hollows");

    private static Path copyOfShipped(Path dir) throws IOException {
        Path copy = dir.resolve("structures.yml");
        Files.copy(SHIPPED.resolve("structures.yml"), copy);
        return copy;
    }

    @Test
    void shippedStructuresRoundTripExactly() throws IOException {
        String original = Files.readString(REAL.resolve("structures.yml"));
        assertEquals(original, StructureFile.read(REAL.resolve("structures.yml")).render());
    }

    @Test
    void shippedWorldAndNucleusRoundTripExactly() throws IOException {
        HollowsWorld world = HollowsWorld.read(SHIPPED.resolve("world.yml"));
        assertNull(world.spawn());
        assertEquals(HollowsWorld.PLACEHOLDER_SPAWN, world.spawnOrPlaceholder());
        assertEquals(Files.readString(SHIPPED.resolve("world.yml")), world.render());
        assertEquals(Files.readString(SHIPPED.resolve("nucleus.yml")),
                NucleusStatueFile.render(NucleusStatueFile.read(SHIPPED.resolve("nucleus.yml"))));
        assertEquals(Files.readString(REAL.resolve("world.yml")), HollowsWorld.read(REAL.resolve("world.yml")).render());
        assertEquals(Files.readString(REAL.resolve("nucleus.yml")),
                NucleusStatueFile.render(NucleusStatueFile.read(REAL.resolve("nucleus.yml"))));
    }

    @Test
    void candidateWritesAreReadBackAndKeepEverythingElse(@TempDir Path dir) throws IOException {
        Path file = copyOfShipped(dir);
        StructureFile document = StructureFile.read(file);
        document.addCandidate("khazad_dum", new Pos(100.25, 40, -300.5, 90, 0));
        document.addCandidate("khazad_dum", new Pos(100.25, 40, -300.5, 90, 0));
        document.write(file);

        StructureSpec spec = StructureCatalogue.load(file).stream().filter(s -> s.id().equals("khazad_dum")).findFirst().orElseThrow();
        assertEquals(List.of(new Pos(100.25, 40, -300.5, 90, 0)), spec.candidates());
        assertEquals("Khazad-dûm", spec.name());
        assertEquals(StructureFile.read(SHIPPED.resolve("structures.yml")).entries().size(), StructureFile.read(file).entries().size());

        StructureFile again = StructureFile.read(file);
        assertEquals(again.render(), Files.readString(file));
        again.clearCandidates("khazad_dum");
        assertEquals(Files.readString(SHIPPED.resolve("structures.yml")), again.render());
    }

    @Test
    void anchorsAreStoredAsOffsetsAndFollowThePlacement(@TempDir Path dir) throws IOException {
        Path file = copyOfShipped(dir);
        StructureFile document = StructureFile.read(file);
        Pos origin = new Pos(1000, 50, 2000, 0, 0);
        document.addCandidate("odawa_hut", origin);
        document.setAnchor("odawa_hut", "odawa", new Pos(3.5, 1, -2, 180, 0));
        document.write(file);

        List<StructureSpec> specs = StructureCatalogue.load(file);
        StructureSpec odawa = specs.stream().filter(s -> s.id().equals("odawa_hut")).findFirst().orElseThrow();
        assertEquals(new Pos(3.5, 1, -2, 180, 0), odawa.anchors().get("odawa"));
        CrystalHollowsLayout.generate(specs, 5L);
        assertEquals(new Pos(1003.5, 51, 1998, 180, 0), CrystalHollowsLayout.anchor("odawa_hut", "odawa"));
        assertNull(CrystalHollowsLayout.anchor("sludge_cage", "sludge"));
    }

    @Test
    void unsetAnchorsAreNotResolvable() {
        StructureSpec khazad = StructureCatalogue.load(SHIPPED.resolve("structures.yml")).stream()
                .filter(s -> s.id().equals("jungle_temple")).findFirst().orElseThrow();
        assertFalse(khazad.anchors().containsKey("door_guardian"));
    }

    @Test
    void unknownStructuresAreRejected() {
        StructureFile document = StructureFile.read(SHIPPED.resolve("structures.yml"));
        assertThrows(IllegalArgumentException.class, () -> document.addCandidate("nowhere", new Pos(0, 0, 0)));
    }

    @Test
    void nearestCandidateIsTheReference() {
        StructureFile document = StructureFile.read(SHIPPED.resolve("structures.yml"));
        document.addCandidate("odawa_hut", new Pos(0, 0, 0));
        document.addCandidate("odawa_hut", new Pos(500, 0, 0));
        assertEquals(new Pos(500, 0, 0), document.find("odawa_hut").nearestCandidate(new Pos(480, 5, 10)));
        assertNull(document.find("sludge_cage").nearestCandidate(new Pos(0, 0, 0)));
    }

    @Test
    void worldAndStatueWritesRoundTrip(@TempDir Path dir) {
        Path worldFile = dir.resolve("world.yml");
        new HollowsWorld(null, null).withSpawn(new Pos(10.5, 80, -4.5, 45, 0)).withBounds(new HollowsWorld.Bounds(200, 201, 800, 802)).write(worldFile);
        HollowsWorld world = HollowsWorld.read(worldFile);
        assertEquals(new Pos(10.5, 80, -4.5, 45, 0), world.spawn());
        assertEquals(new HollowsWorld.Bounds(200, 201, 800, 802), world.bounds());

        Path nucleus = dir.resolve("nucleus.yml");
        NucleusStatueFile.set(nucleus, NucleusCrystal.JADE, 1, 2, 3);
        NucleusStatueFile.set(nucleus, NucleusCrystal.TOPAZ, 4, 5, 6);
        NucleusStatueFile.set(nucleus, NucleusCrystal.JADE, 7, 8, 9);
        Map<NucleusCrystal, int[]> statues = NucleusStatueFile.read(nucleus);
        assertEquals(2, statues.size());
        assertEquals(7, statues.get(NucleusCrystal.JADE)[0]);
        assertEquals(6, statues.get(NucleusCrystal.TOPAZ)[2]);
    }

    @Test
    void regionPlaceholdersAreDetectedAfterNormalising() {
        assertTrue(HollowsRegions.isPlaceholder("crystal_hollows_nucleus", new HollowsRegions.Box(562, 562, 462, 462).normalised()));
        assertFalse(HollowsRegions.isPlaceholder("crystal_hollows_nucleus", new HollowsRegions.Box(400, 400, 600, 600)));
        assertTrue(HollowsRegions.ids().contains("crystal_hollows_khazad_dum"));
        assertTrue(HollowsRegions.ids().contains("crystal_hollows"));
    }

    private static HollowsValidator.Input input(List<StructureSpec> specs, Set<NucleusCrystal> statues,
                                                Map<String, HollowsRegions.Box> regions, HollowsWorld world, Set<String> placed) {
        return new HollowsValidator.Input(specs, HollowsAnchors.all(), statues, regions, world, placed);
    }

    @Test
    void validateReportsEverythingOnTheShippedFiles() {
        List<StructureSpec> specs = StructureCatalogue.load(SHIPPED.resolve("structures.yml"));
        var issues = HollowsValidator.validate(input(specs, Set.of(), Map.of(), new HollowsWorld(null, null), Set.of()));
        String all = issues.stream().map(HollowsValidator.Issue::message).reduce("", (a, b) -> a + "\n" + b);
        assertTrue(all.contains("Structure jungle_temple has no candidates"));
        assertTrue(all.contains("Anchor jungle_temple/door_guardian is not set"));
        assertTrue(all.contains("Nucleus statue for AMBER is not set"));
        assertTrue(all.contains("Spawn is unset"));
        assertTrue(all.contains("Region crystal_hollows_jungle is not defined"));
        assertTrue(issues.stream().anyMatch(i -> i.level() == HollowsValidator.Level.WARNING && i.message().contains("fairy_grotto_1")));
    }

    @Test
    void validateIsCleanWhenEverythingIsFilledIn(@TempDir Path dir) throws IOException {
        Path file = copyOfShipped(dir);
        StructureFile document = StructureFile.read(file);
        for (StructureFile.Entry entry : document.entries()) {
            document.addCandidate(entry.id(), new Pos(entry.id().hashCode() % 1000, 60, 0));
        }
        for (HollowsAnchors.Ref ref : HollowsAnchors.all()) {
            document.setAnchor(ref.structure(), ref.anchor(), new Pos(1, 2, 3));
        }
        document.write(file);
        List<StructureSpec> specs = StructureCatalogue.load(file);

        Set<NucleusCrystal> statues = Set.of(NucleusCrystal.values());
        Map<String, HollowsRegions.Box> regions = new java.util.HashMap<>();
        HollowsRegions.ids().forEach(id -> regions.put(id, new HollowsRegions.Box(1, 1, 2, 2)));
        HollowsWorld world = new HollowsWorld(new Pos(0, 100, 0), new HollowsWorld.Bounds(0, 0, 10, 10));
        Set<String> placed = Set.copyOf(specs.stream().map(StructureSpec::id).toList());

        assertEquals(List.of(), HollowsValidator.validate(input(specs, statues, regions, world, placed)));
    }

    @Test
    void everyStructureAnchorUsedByNpcsAndBossesIsInTheRegistry() throws IOException {
        Set<String> structureIds = Set.copyOf(StructureCatalogue.load(SHIPPED.resolve("structures.yml")).stream().map(StructureSpec::id).toList());
        Pattern pair = Pattern.compile("\"([a-z_]+)\",\\s*\"([a-z_]+)\"");
        Set<String> known = Set.copyOf(HollowsAnchors.all().stream().map(ref -> ref.structure() + "/" + ref.anchor()).toList());
        int found = 0;
        for (String directory : List.of("npcs", "boss")) {
            Path root = Path.of("src/main/java/net/swofty/type/crystalhollows", directory);
            try (Stream<Path> files = Files.list(root)) {
                for (Path source : files.toList()) {
                    if (source.getFileName().toString().equals("AbstractKeeper.java")) continue;
                    Matcher matcher = pair.matcher(Files.readString(source));
                    while (matcher.find()) {
                        if (!structureIds.contains(matcher.group(1))) continue;
                        found++;
                        assertTrue(known.contains(matcher.group(1) + "/" + matcher.group(2)),
                                source.getFileName() + " uses an anchor missing from HollowsAnchors: " + matcher.group());
                    }
                }
            }
        }
        assertTrue(found >= 10);
    }
}
