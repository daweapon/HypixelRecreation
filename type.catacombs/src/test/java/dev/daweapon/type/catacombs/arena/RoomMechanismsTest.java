package dev.daweapon.type.catacombs.arena;

import dev.daweapon.dungeons.catacombs.layout.CellGeometry;
import dev.daweapon.dungeons.catacombs.layout.FloorLayout;
import dev.daweapon.dungeons.catacombs.layout.LayoutDoor;
import dev.daweapon.dungeons.catacombs.layout.RoomStructure;
import dev.daweapon.dungeons.catacombs.layout.RoomTemplate;
import dev.daweapon.dungeons.catacombs.layout.Rotation;
import dev.daweapon.type.catacombs.secret.SecretLevers;
import dev.daweapon.type.catacombs.secret.SuperboomWalls;
import dev.daweapon.type.skyblockgeneric.schematic.Schematic;
import net.minestom.server.instance.block.Block;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomMechanismsTest {
    private static RoomMechanisms mechanisms;
    private static DoorPieces pieces;
    private static Map<String, RoomTemplate> templates;

    @BeforeAll
    static void load() {
        RoomDoorwaysTest.load();
        mechanisms = RoomMechanisms.load(RoomDoorwaysTest.ROOMS.getParent().resolve("room_mechanisms.json"));
        pieces = DoorPieces.load(RoomDoorwaysTest.ROOMS.getParent().resolve("door_pieces.json"));
        templates = RoomDoorwaysTest.library().all().stream().collect(Collectors.toMap(RoomTemplate::id, template -> template));
    }

    private static Block local(RoomTemplate template, int[] at) {
        RoomStructure structure = template.blocks().orElseThrow();
        Schematic schematic = RoomDoorwaysTest.structures().load(structure);
        int x = at[0] - structure.x(), y = at[1] - structure.y(), z = at[2] - structure.z();
        if (x < 0 || y < 0 || z < 0 || x >= schematic.width() || y >= schematic.height() || z >= schematic.length()) return null;
        return schematic.blockAt(x, y, z);
    }

    @Test
    void theCapturedTrapLeverOpensItsFloorPatchAndLogWall() {
        RoomMechanisms.Lever lever = mechanisms.levers("trap").stream().filter(each -> each.label().equals("1 - Lever Behind Crypt")).findFirst().orElseThrow();
        assertEquals("capture", lever.source());
        assertEquals(33, lever.opens().size());
        assertEquals(16, lever.opens().stream().filter(at -> local(templates.get("trap"), at).name().equals("minecraft:oak_log")).count());
    }

    @Test
    void theLeversSeenInSecretGuidesOpenTheirDoors() {
        Map<String, Map<String, Integer>> expected = Map.of(
                "atlas|2 - Lever", Map.of("minecraft:oak_log", 24),
                "museum|1 - Lever", Map.of("minecraft:oak_wood", 26, "minecraft:spruce_stairs", 18, "minecraft:oak_log", 1),
                "chambers|3 - Lever", Map.of("minecraft:spruce_log", 8),
                "hallway|2 - Lever", Map.of("minecraft:oak_log", 16),
                "grass_ruin|3 - Lever 1", Map.of("minecraft:oak_wood", 31, "minecraft:spruce_stairs", 26, "minecraft:oak_log", 2),
                "grass_ruin|3 - Lever 2", Map.of("minecraft:iron_bars", 33),
                "buttons|3 - Lever behind Crypt", Map.of("minecraft:oak_log", 27));
        for (Map.Entry<String, Map<String, Integer>> entry : expected.entrySet()) {
            String[] key = entry.getKey().split("\\|");
            RoomMechanisms.Lever lever = mechanisms.levers(key[0]).stream().filter(each -> each.label().equals(key[1])).findFirst().orElseThrow();
            assertEquals("video", lever.source(), entry.getKey());
            Map<String, Integer> kinds = new java.util.TreeMap<>();
            for (int[] at : lever.opens()) kinds.merge(local(templates.get(key[0]), at).name(), 1, Integer::sum);
            assertEquals(new java.util.TreeMap<>(entry.getValue()), kinds, entry.getKey());
        }
    }

    @Test
    void theMuseumDoorShutsAPlayerOutOfTheChestRoom() {
        RoomTemplate template = templates.get("museum");
        for (Rotation rotation : RoomDoorwaysTest.rotations(template)) {
            FloorLayout layout = RoomDoorwaysTest.surrounded(template, rotation, true);
            CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, RoomDoorwaysTest.structures(), false, pieces);
            RoomMechanisms.Lever lever = SecretLevers.placed(layout, blocks::isReal, mechanisms).stream()
                    .filter(each -> each.lever().label().equals("1 - Lever")).findFirst().orElseThrow().lever();
            assertFalse(reaches(blocks, layout, lever, Set.of(), 2), "museum " + rotation + " chest reachable before the pull");
            assertTrue(reaches(blocks, layout, lever, packed(lever.opens()), 2), "museum " + rotation + " chest still shut after the pull");
        }
    }

    @Test
    void theGrassRuinChestNeedsBothLevers() {
        RoomTemplate template = templates.get("grass_ruin");
        for (Rotation rotation : RoomDoorwaysTest.rotations(template)) {
            FloorLayout layout = RoomDoorwaysTest.surrounded(template, rotation, true);
            CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, RoomDoorwaysTest.structures(), false, pieces);
            List<SecretLevers.PlacedLever> placed = SecretLevers.placed(layout, blocks::isReal, mechanisms);
            RoomMechanisms.Lever first = placed.stream().filter(each -> each.lever().label().equals("3 - Lever 1")).findFirst().orElseThrow().lever();
            RoomMechanisms.Lever second = placed.stream().filter(each -> each.lever().label().equals("3 - Lever 2")).findFirst().orElseThrow().lever();
            String context = "grass_ruin " + rotation;
            assertFalse(reaches(blocks, layout, first, Set.of()), context + " chest reachable with no lever pulled");
            assertFalse(reaches(blocks, layout, first, packed(first.opens())), context + " chest reachable with only lever 1 pulled");
            assertFalse(reaches(blocks, layout, first, packed(second.opens())), context + " chest reachable with only lever 2 pulled");
            Set<Long> both = packed(first.opens());
            both.addAll(packed(second.opens()));
            assertTrue(reaches(blocks, layout, first, both), context + " chest still shut after both levers");
        }
    }

    private static Set<Long> packed(List<int[]> blocks) {
        Set<Long> out = new HashSet<>();
        for (int[] at : blocks) out.add(CatacombsArenaBlocks.pack(at[0], at[1], at[2]));
        return out;
    }

    @Test
    void everyLeverAndSuperboomWallSitsOnItsBlocksInEveryRotation() {
        int levers = 0, walls = 0;
        for (RoomTemplate template : templates.values()) {
            if (template.blocks().isEmpty()) continue;
            List<RoomMechanisms.Lever> ownLevers = mechanisms.levers(template.id());
            List<RoomMechanisms.Wall> ownWalls = mechanisms.walls(template.id());
            if (ownLevers.isEmpty() && ownWalls.isEmpty()) continue;
            for (RoomMechanisms.Lever lever : ownLevers) {
                Block at = local(template, lever.at());
                assertTrue(at == null || at.air() || at.compare(Block.LEVER), template.id() + " " + lever.label() + " sits on " + at);
                for (int[] block : lever.opens()) {
                    Block opened = local(template, block);
                    assertTrue(opened != null && !opened.air(), template.id() + " " + lever.label() + " opens air");
                }
            }
            for (RoomMechanisms.Wall wall : ownWalls) {
                long cracked = wall.blocks().stream().filter(block -> local(template, block) != null && local(template, block).compare(Block.CRACKED_STONE_BRICKS)).count();
                assertTrue(cracked * 4 >= wall.blocks().size() * 3L, template.id() + " " + wall.label() + " is " + cracked + " of " + wall.blocks().size() + " cracked");
                assertTrue(wall.blocks().size() <= 64, template.id() + " " + wall.label());
            }
            for (Rotation rotation : RoomDoorwaysTest.rotations(template)) {
                FloorLayout layout = RoomDoorwaysTest.surrounded(template, rotation, false);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, RoomDoorwaysTest.structures(), false, pieces);
                String context = template.id() + " " + rotation;
                for (SecretLevers.PlacedLever placed : SecretLevers.placed(layout, blocks::isReal, mechanisms)) {
                    RoomMechanisms.Lever original = ownLevers.stream().filter(each -> each.label().equals(placed.lever().label())).findFirst().orElseThrow();
                    Block localLever = local(template, original.at());
                    if (localLever != null && localLever.compare(Block.LEVER)) {
                        Block shown = blocks.blockAt(placed.lever().at()[0], placed.lever().at()[1], placed.lever().at()[2]);
                        assertTrue(shown != null && shown.compare(Block.LEVER), context + " lever " + original.label() + " shows " + shown);
                    }
                    for (int i = 0; i < original.opens().size(); i++) {
                        int[] world = placed.lever().opens().get(i);
                        Block shown = blocks.blockAt(world[0], world[1], world[2]);
                        assertEquals(local(template, original.opens().get(i)).name(), shown == null ? null : shown.name(), context + " " + original.label());
                    }
                    levers++;
                }
                for (SuperboomWalls.PlacedWall placed : SuperboomWalls.placed(layout, blocks::isReal, mechanisms)) {
                    RoomMechanisms.Wall original = ownWalls.stream().filter(each -> each.label().equals(placed.wall().label())).findFirst().orElseThrow();
                    for (int i = 0; i < original.blocks().size(); i++) {
                        int[] world = placed.wall().blocks().get(i);
                        Block shown = blocks.blockAt(world[0], world[1], world[2]);
                        assertEquals(local(template, original.blocks().get(i)).name(), shown == null ? null : shown.name(), context + " " + original.label());
                    }
                    walls++;
                }
            }
        }
        assertTrue(levers > 50 && walls > 100, levers + " lever and " + walls + " wall placements");
    }

    @Test
    void aLeverThatGuardsItsSecretOpensTheWayToIt() {
        int checked = 0;
        for (RoomTemplate template : templates.values()) {
            for (RoomMechanisms.Lever lever : mechanisms.levers(template.id())) {
                if (!lever.freesSecret()) continue;
                FloorLayout layout = RoomDoorwaysTest.surrounded(template, RoomDoorwaysTest.rotations(template).getFirst(), true);
                CatacombsArenaBlocks blocks = CatacombsArenaBlocks.of(layout, RoomDoorwaysTest.structures(), false, pieces);
                SecretLevers.PlacedLever placed = SecretLevers.placed(layout, blocks::isReal, mechanisms).stream()
                        .filter(each -> each.lever().label().equals(lever.label())).findFirst().orElseThrow();
                String context = template.id() + " " + lever.label();
                assertFalse(reaches(blocks, layout, placed.lever(), Set.of()), context + " is reachable before the pull");
                Set<Long> opened = new HashSet<>();
                for (int[] at : placed.lever().opens()) opened.add(CatacombsArenaBlocks.pack(at[0], at[1], at[2]));
                assertTrue(reaches(blocks, layout, placed.lever(), opened), context + " is still shut after the pull");
                checked++;
            }
        }
        assertTrue(checked >= 20, checked + " levers checked");
    }

    private static boolean reaches(CatacombsArenaBlocks blocks, FloorLayout layout, RoomMechanisms.Lever lever, Set<Long> opened) {
        return reaches(blocks, layout, lever, opened, 1);
    }

    /** Whether open air, {@code height} blocks tall, connects a doorway of the room to a block next to the lever's secret. */
    private static boolean reaches(CatacombsArenaBlocks blocks, FloorLayout layout, RoomMechanisms.Lever lever, Set<Long> opened, int height) {
        Set<Long> goals = new HashSet<>();
        for (int[] secret : lever.secretBlocks()) {
            for (int[] step : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) {
                goals.add(CatacombsArenaBlocks.pack(secret[0] + step[0], secret[1] + step[1], secret[2] + step[2]));
            }
        }
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        Set<Long> seen = new HashSet<>();
        for (LayoutDoor door : layout.doors()) {
            CellGeometry.BlockPosition seam = CellGeometry.doorPosition(door.from(), door.to());
            queue.add(new int[]{seam.x(), CellGeometry.FLOOR_Y + 1, seam.z()});
        }
        int budget = 400_000;
        while (!queue.isEmpty() && budget-- > 0) {
            int[] at = queue.poll();
            long key = CatacombsArenaBlocks.pack(at[0], at[1], at[2]);
            if (!seen.add(key)) continue;
            if (goals.contains(key)) return true;
            for (int[] step : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) {
                int x = at[0] + step[0], y = at[1] + step[1], z = at[2] + step[2];
                if (y < blocks.minY() || y > blocks.maxY() || !blocks.plan().roomAtColumn(x, z).map(room -> room.index() == 0).orElse(false)) continue;
                boolean open = true;
                for (int above = 0; above < height && open; above++) open = open(blocks, opened, x, y + above, z);
                if (open && !seen.contains(CatacombsArenaBlocks.pack(x, y, z))) queue.add(new int[]{x, y, z});
            }
        }
        return false;
    }

    private static boolean open(CatacombsArenaBlocks blocks, Set<Long> opened, int x, int y, int z) {
        Block block = blocks.blockAt(x, y, z);
        return block == null || !block.solid() || opened.contains(CatacombsArenaBlocks.pack(x, y, z));
    }
}
