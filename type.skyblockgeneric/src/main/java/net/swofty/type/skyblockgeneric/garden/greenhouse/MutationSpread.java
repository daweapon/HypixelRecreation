package net.swofty.type.skyblockgeneric.garden.greenhouse;

import net.swofty.type.skyblockgeneric.garden.GardenProfile;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

public final class MutationSpread {
    private static final String FIRE = "FIRE";

    private MutationSpread() {
    }

    public static void onStage(SkyBlockPlayer player, GardenProfile profile, int plot, GreenhouseData.Bed bed, long now) {
        roll(bed, now, GreenhouseRules.random());
    }

    public static List<Integer> roll(GreenhouseData.Bed bed, long now, RandomGenerator random) {
        GreenhouseConfig config = GreenhouseConfig.get();
        List<Integer> spawned = new ArrayList<>();
        List<Integer> empty = new ArrayList<>();
        for (int slot = 0; slot < GreenhouseData.SLOTS; slot++) {
            if (bed.getUnlocked()[slot] && !bed.occupied(slot)) empty.add(slot);
        }
        Collections.shuffle(empty, new java.util.Random(random.nextLong()));
        List<MutationCatalogue.Mutation> candidates = new ArrayList<>(MutationCatalogue.get().all());
        Map<Integer, String> placed = new HashMap<>();
        for (int slot : empty) {
            Collections.shuffle(candidates, new java.util.Random(random.nextLong()));
            for (MutationCatalogue.Mutation mutation : candidates) {
                if (!eligible(mutation, bed, slot, placed)) continue;
                if (random.nextDouble() * 100 >= mutation.chancePercent()) continue;
                placed.put(slot, mutation.id());
                spawned.add(slot);
                break;
            }
        }
        placed.forEach((slot, id) -> {
            GreenhouseData.Plant plant = new GreenhouseData.Plant(id, now);
            if (config.plant(id) != null && config.plant(id).stages() == 0) plant.setGrownAt(now);
            bed.getPlants().put(slot, plant);
        });
        return spawned;
    }

    public static boolean eligible(MutationCatalogue.Mutation mutation, GreenhouseData.Bed bed, int anchor, Map<Integer, String> placed) {
        if (mutation.chancePercent() <= 0) return false;
        int size = mutation.footprint();
        if (!footprintFree(mutation, bed, anchor, placed)) return false;
        return switch (mutation.mode()) {
            case "ADJACENT_ALL" -> conditionsMet(mutation, neighbourTypes(bed, anchor, size));
            case "UNIQUE_BASE" -> GreenhouseRules.godseedSatisfied(neighbourTypes(bed, anchor, size), GreenhouseConfig.get());
            case "SPECIAL" -> mutation.id().equals("LONELILY") && !bed.getPlants().isEmpty() && neighbourTypes(bed, anchor, size).isEmpty();
            default -> false;
        };
    }

    static boolean footprintFree(MutationCatalogue.Mutation mutation, GreenhouseData.Bed bed, int anchor, Map<Integer, String> placed) {
        List<Integer> cells = Footprint.cells(anchor, mutation.footprint());
        if (cells.isEmpty()) return false;
        Set<Integer> taken = new HashSet<>();
        GreenhouseConfig config = GreenhouseConfig.get();
        placed.forEach((slot, id) -> {
            PlantType type = config.plant(id);
            taken.addAll(Footprint.cells(slot, type == null ? 1 : type.footprint()));
        });
        for (int cell : cells) {
            if (!bed.getUnlocked()[cell] || bed.occupied(cell) || taken.contains(cell)) return false;
            if (!Surface.accepts(mutation.surface(), bed.surface(cell))) return false;
        }
        return true;
    }

    static List<String> neighbourTypes(GreenhouseData.Bed bed, int anchor, int size) {
        List<String> types = new ArrayList<>();
        for (int cell : Footprint.ring(anchor, size)) {
            GreenhouseData.Plant plant = bed.plantAt(cell);
            if (plant != null) types.add(plant.getType());
        }
        return types;
    }

    static boolean conditionsMet(MutationCatalogue.Mutation mutation, List<String> neighbours) {
        List<MutationCatalogue.Condition> required = new ArrayList<>();
        for (MutationCatalogue.Condition condition : mutation.conditions()) {
            if (!condition.crop().equals(FIRE)) required.add(condition);
        }
        if (required.isEmpty()) return false;
        Map<String, Integer> available = new HashMap<>();
        neighbours.forEach(type -> available.merge(type, 1, Integer::sum));
        Map<String, Integer> needed = new HashMap<>();
        for (MutationCatalogue.Condition condition : required) needed.merge(normalise(condition.crop()), condition.count(), Integer::sum);
        for (Map.Entry<String, Integer> need : needed.entrySet()) {
            if (available.getOrDefault(need.getKey(), 0) < need.getValue()) return false;
        }
        return true;
    }

    private static String normalise(String crop) {
        return switch (crop) {
            case "RED_MUSHROOM", "BROWN_MUSHROOM" -> "MUSHROOM";
            case "MELON_SLICE" -> "MELON";
            default -> crop;
        };
    }
}
