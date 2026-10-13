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
    public static final int MAX_NEIGHBOURS = 8;
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
            if (bed.getUnlocked()[slot] && !bed.getPlants().containsKey(slot)) empty.add(slot);
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

    public static boolean eligible(MutationCatalogue.Mutation mutation, GreenhouseData.Bed bed, int slot, Map<Integer, String> placed) {
        if (mutation.chancePercent() <= 0) return false;
        return switch (mutation.mode()) {
            case "ADJACENT_ALL" -> conditionsMet(mutation, neighbourTypes(bed, slot, Map.of()));
            case "UNIQUE_BASE" -> uniqueBase(neighbourTypes(bed, slot, Map.of())) >= 12 / 2;
            case "SPECIAL" -> mutation.id().equals("LONELILY") && !bed.getPlants().isEmpty() && isolated(bed, slot, Map.of());
            default -> false;
        };
    }

    static List<String> neighbourTypes(GreenhouseData.Bed bed, int slot, Map<Integer, String> placed) {
        List<String> types = new ArrayList<>();
        for (int[] cell : GreenhouseLayout.around(slot)) {
            int other = GreenhouseData.slot(cell[0], cell[1]);
            GreenhouseData.Plant plant = bed.getPlants().get(other);
            String type = plant != null ? plant.getType() : placed.get(other);
            if (type != null) types.add(type);
        }
        return types;
    }

    static boolean conditionsMet(MutationCatalogue.Mutation mutation, List<String> neighbours) {
        List<MutationCatalogue.Condition> required = new ArrayList<>();
        for (MutationCatalogue.Condition condition : mutation.conditions()) {
            if (!condition.crop().equals(FIRE)) required.add(condition);
        }
        if (required.isEmpty()) return false;
        int total = required.stream().mapToInt(MutationCatalogue.Condition::count).sum();
        double scale = total > MAX_NEIGHBOURS ? MAX_NEIGHBOURS / (double) total : 1;
        Map<String, Integer> available = new HashMap<>();
        neighbours.forEach(type -> available.merge(type, 1, Integer::sum));
        for (MutationCatalogue.Condition condition : required) {
            int need = Math.max(1, (int) Math.floor(condition.count() * scale));
            if (available.getOrDefault(normalise(condition.crop()), 0) < need) return false;
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

    static int uniqueBase(List<String> neighbours) {
        Set<String> unique = new HashSet<>();
        for (String type : neighbours) {
            PlantType plant = GreenhouseConfig.get().plant(type);
            if (plant != null && plant.baseCrop()) unique.add(type);
        }
        return unique.size();
    }

    static boolean isolated(GreenhouseData.Bed bed, int slot, Map<Integer, String> placed) {
        return neighbourTypes(bed, slot, Map.of()).isEmpty();
    }
}
