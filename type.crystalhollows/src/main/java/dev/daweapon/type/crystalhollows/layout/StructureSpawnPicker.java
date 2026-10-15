package dev.daweapon.type.crystalhollows.layout;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.region.MobFreeZones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class StructureSpawnPicker {
    private static final int COLUMNS_PER_BOX = 40;
    private static final int HEADROOM = 2;

    private StructureSpawnPicker() {
    }

    public interface Terrain {
        boolean isSolid(int x, int y, int z);

        boolean isAir(int x, int y, int z);
    }

    public record Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public static Box of(StructurePlacement placement) {
            StructureSite site = placement.spec().site();
            Pos origin = placement.origin();
            return new Box(origin.blockX(), origin.blockY(), origin.blockZ(),
                    origin.blockX() + site.width() - 1, origin.blockY() + site.height() - 1, origin.blockZ() + site.length() - 1);
        }
    }

    public static List<Box> eligibleBoxes(Iterable<StructurePlacement> placements) {
        List<Box> boxes = new ArrayList<>();
        for (StructurePlacement placement : placements) {
            if (placement.spec().site() == null || MobFreeZones.isMobFree(placement.spec().region())) continue;
            boxes.add(Box.of(placement));
        }
        return boxes;
    }

    public static Optional<Pos> pick(List<Box> boxes, Terrain terrain, Random random) {
        List<Box> order = new ArrayList<>(boxes);
        Collections.shuffle(order, random);
        for (Box box : order) {
            Optional<Pos> found = pickIn(box, terrain, random);
            if (found.isPresent()) return found;
        }
        return Optional.empty();
    }

    public static Optional<Pos> pickIn(Box box, Terrain terrain, Random random) {
        for (int attempt = 0; attempt < COLUMNS_PER_BOX; attempt++) {
            int x = box.minX() + random.nextInt(box.maxX() - box.minX() + 1);
            int z = box.minZ() + random.nextInt(box.maxZ() - box.minZ() + 1);
            List<Integer> floors = standableFloors(box, terrain, x, z);
            if (floors.isEmpty()) continue;
            return Optional.of(new Pos(x + 0.5, floors.get(random.nextInt(floors.size())), z + 0.5));
        }
        return Optional.empty();
    }

    private static List<Integer> standableFloors(Box box, Terrain terrain, int x, int z) {
        List<Integer> floors = new ArrayList<>();
        for (int y = box.minY() + 1; y + HEADROOM - 1 <= box.maxY(); y++) {
            if (!terrain.isSolid(x, y - 1, z)) continue;
            boolean open = true;
            for (int h = 0; h < HEADROOM && open; h++) open = terrain.isAir(x, y + h, z);
            if (open) floors.add(y);
        }
        return floors;
    }
}
